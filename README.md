# StalkerVerse

**Privacy-first Chromium browser with native Web3 naming**

```
Chromium (Blink · V8 · Network · Sandbox)
        │
        ▼
StalkerVerse Layer
├── Web3 Domain Engine   (ENS · HNS · UNS · SNS · TON · Tezos · Namecoin · EmerDNS)
├── DNS Privacy Engine   (AdGuard DoH / DoT / DoQ — Web2 only)
├── DApp Bridge          (EIP-1193 / EIP-6963)
├── Wallet Vault         (Android Keystore, local only)
├── Privacy Controls     (anti-FP, ECH/QUIC, no telemetry)
├── Battery Optimizer
└── Multilingual UI      (EN / RU / BG)
```

Not a Brave fork. Not a WebView wrapper. Clean Chromium overlay + modular StalkerVerse layer.

## Status

| Phase | Goal | State |
|-------|------|--------|
| **0** | Pinned Chromium, build scripts, empty overlay | **in progress** |
| **1** | Full browser UI (tabs, omnibox, history, bookmarks, settings, i18n) | preview done |
| **2** | Namespace detector + Web3 resolve (before public DNS) | engine ready |
| **3** | Encrypted DNS profiles + privacy controls | profiles defined |
| **4** | DApp bridge | planned |
| **5** | Local wallet vault | planned |
| **6** | Contextual ads + stablecoin escrow (off by default) | planned |
| **7** | Battery-aware architecture | planned |
| **8** | CI APK on large/self-hosted runner | scripts ready |

**Pinned Chromium:** `154.0.8037.126`  
**Android target:** `chrome_public_apk`

## Hard rules

1. **Classify hostname before any public DNS call.** `.eth` never hits AdGuard/system DNS.
2. **No IP rewrite** for unresolved Web3 names.
3. **Contenthash → IPFS/IPNS/Arweave** with explicit gateway trust banner.
4. **No telemetry.** No private keys leave the device.
5. **No DPI bypass in V1** — network privacy = encrypted DNS, ECH/QUIC, anti-tracking only.

## Quick start (overlay + docs)

```bash
git clone https://github.com/zametkikostik/StalkerVerse.git
cd StalkerVerse
# Phase 0 Chromium fetch (needs ~600 GB disk, 64 GB RAM recommended)
./scripts/fetch-chromium.sh
./scripts/apply-overlay.sh
./scripts/build-apk.sh
```

## Web3 resolve (TypeScript reference)

Pure client classification lives in `src/lib/names/`.  
RPC resolve (ENS via Ethereum public nodes) is server-side or native in the final APK.

```ts
classifyHostname("uniswap.eth")  // → { path: "web3", provider: "ens" }
// never calls public DNS

classifyHostname("example.com")  // → { path: "web2" }
// only then AdGuard DoH / system DNS
```

## License inventory

- Chromium upper layers: **BSD 3-Clause**
- StalkerVerse layer: **MPL-2.0** (this repo)
- Keep a clean SBOM separate from Brave Core (MPL) to avoid product coupling

## Docs

- [ARCHITECTURE.md](docs/ARCHITECTURE.md)
- [DOMAIN-RESOLUTION.md](docs/DOMAIN-RESOLUTION.md)
- [PRIVACY-MODEL.md](docs/PRIVACY-MODEL.md)
- [ROADMAP.md](docs/ROADMAP.md)
- [GROK-MASTER-PROMPT.md](docs/GROK-MASTER-PROMPT.md)

## Contributing

Phase order is strict: **buildable APK before more Web3 features.**  
PRs that break “namespace before DNS” will be rejected.
