# Recipe: 0003-navigation-interceptor

**Pin:** Chromium `154.0.8037.126`  
**Depends on:** portable classifier in `android/java/.../NamespaceClassifier.java` + JNI

## Intent

Gate **every** user-initiated navigation hostname through StalkerVerse classification **before** `net::HostResolver` resolves the original name via system/public DNS.

## Algorithm (must match `src/lib/names/detector.ts`)

```
input → canonicalize
     → matchProviderId (ENS, UNS, …, HNS)
     → if provider: path=web3, resolve via provider, never HostResolver(name)
     → else: path=web2, existing DNS + optional DoH profile
```

## Suggested injection points

### A. Android omnibox (preferred first)

Locate omnibox navigation commit (search `LoadUrlParams`, `OmniboxNavigationObserver`, or `ChromeTabbedActivity` load paths under `chrome/android`).

Insert:

```text
Classification c = NamespaceClassifier.classify(text);
if (c.path == WEB3) {
  ResolveResult r = NamespaceClassifier.resolveWeb3(c.hostname, c.providerId);
  if (r.target != null) loadUrl(r.target.url, r.trust);
  else showWeb3Error(r);
  return; // do not continue to default DNS fixup
}
// else fall through to Chromium Web2 path
```

### B. Safety net in browser process

Optional: in navigation start, if request host matches Web3 TLD tables and was not pre-resolved, abort DNS and re-enter resolve path.

## Files to add (overlay, not upstream Chromium)

```
android/java/org/stalkerverse/names/NamespaceClassifier.java
android/java/org/stalkerverse/names/Classification.java
android/java/org/stalkerverse/names/ResolveResult.java
android/java/org/stalkerverse/names/TldTables.java
```

Wire into GN target for `chrome_public_apk` via overlay `BUILD.gn` snippets documented in `android/BUILD.overlay.md`.

## Tests

- Unit: same cases as `src/lib/names/detector.test.ts`
- Manual: omnibox `uniswap.eth` → no query to `dns.adguard-dns.com` for name `uniswap.eth`
- Manual: `example.com` → DoH path still works

## Generating the real .patch

On the build machine after sources are integrated:

```bash
cd $CHROMIUM_SRC
git add -A
git diff --cached > /path/to/StalkerVerse/chromium/patches/0003-navigation-interceptor.patch
```
