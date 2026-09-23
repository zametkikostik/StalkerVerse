# StalkerVerse — Web3 Chromium Browser

Web3 mobile browser stack: Chromium-oriented architecture with Web3 provider, ENS/UD domains, DPI protection, HD wallet + WalletConnect, on-device ads.

**Repo:** https://github.com/zametkikostik/StalkerVerse

## What's included

| Area | Path |
|------|------|
| Android shell | `android/` — MainActivity, Compose UI, DPI Service, Wallet |
| Web3 provider | `modules/web3/ethereum_provider.js` |
| Domain resolver | `modules/domain-resolver/` |
| DPI proxy | `modules/dpi-proxy/dpi_proxy.py` |
| Chromium guides | `patches/`, `scripts/` |
| Docs | `docs/` |

## Quick start (Android debug)

```bash
cd android
cp local.properties.example local.properties
# set sdk.dir and reown.project.id
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Chromium full build

```bash
./scripts/fetch_and_patch_chromium.sh ~/chromium
cd ~/chromium/src && gn args out/Web3Default  # see scripts/args.gn.template
autoninja -C out/Web3Default chrome_public_apk
```

## License

Apache-2.0 (project code). Chromium is BSD-3-Clause.
