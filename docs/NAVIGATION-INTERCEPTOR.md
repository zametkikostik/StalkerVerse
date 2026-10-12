# Navigation interceptor — classify before system DNS

## Goal

When the user submits an omnibox string or a page navigates to a hostname:

1. **Canonicalize** the hostname (local).
2. **Classify** via StalkerVerse namespace rules (local, pure).
3. If **Web3** → resolve via provider RPC / dedicated API; **never** call system DNS or AdGuard for that name.
4. If **Web2** → encrypted DNS profile (AdGuard DoH/DoT/DoQ) or system DNS.
5. Build `NavigationTarget` + trust label; load or show honest failure.

## Chromium hook points (Android, ~154)

| Layer | File / component | Role |
|-------|------------------|------|
| Omnibox accept | `chrome/browser/ui/android/omnibox/` | Call classifier before URL fixup commits DNS |
| Navigation start | `content/browser/renderer_host/navigation_request.cc` | Optional safety net |
| DNS resolution | `net/dns/host_resolver_*` | Skip for Web3-classified hosts |
| Custom scheme / rewrite | `chrome/browser/net/` or Android `Tab` loadUrl | Load gateway URL with trust banner |

Recommended **first** hook: Android omnibox / `LoadUrlParams` path in the browser process, *before* `HostResolver` is asked for the original hostname.

```
Omnibox submit
    → StalkerVerse::Classify(host)     // JNI → pure C++/Kotlin
    → if web3: StalkerVerse::Resolve() // RPC, cache TTL
    → if target: LoadUrl(target.url) + show trust chip
    → if web2: existing Chromium DNS with DoH profile
```

## JNI surface (proposed)

```java
// org.stalkerverse.names.NamespaceClassifier
public final class NamespaceClassifier {
  /** Pure local classification. No network. */
  public static native Classification classify(String input);

  /** Web3 resolve; may hit RPC. Never public DNS for .eth etc. */
  public static native ResolveResult resolveWeb3(String hostname, String providerId);
}
```

Mirror of TypeScript:

- `src/lib/names/detector.ts` → Kotlin/C++ port
- `src/lib/names/match.ts` → same TLD tables
- ENS resolve → native library or JSON-RPC with publicnode / llama / cloudflare fallbacks

## Trust UI

| Trust | Chrome UI |
|-------|-----------|
| `gateway` | Persistent strip: “Gateway mode — not trustless direct” + URL |
| `dns` | Normal lock / HTTPS UI |
| `direct` | Optional “content-addressed” chip |

## Failure modes (honest)

- ENS name exists, only `addr`, no contenthash/url → show records, **do not** open `http://<ip>/`
- HNS resolve fails → error page, **no** fallback to public DNS for that TLD
- RPC all down → error with endpoint list from Settings → Privacy

## Patch files (this repo)

See `chromium/patches/`:

1. `0001-branding-stalkerverse.patch` — product strings (generated against pin)
2. `0002-dns-profile-hooks.patch` — Web2 DoH profile selection
3. `0003-navigation-interceptor.patch` — omnibox / loadUrl classify gate

Until a full Chromium tree is available on a builder, patches are **recipes** in `*.recipe.md` plus portable classifier sources under `android/` and `src/lib/names/`.
