# StalkerVerse — Web3 Chromium Browser

Web3 mobile browser stack: Chromium-oriented architecture with Web3 provider, ENS/UD domains, DPI protection, HD wallet + WalletConnect, on-device ads.

**Repo:** https://github.com/zametkikostik/StalkerVerse

## CI — automatic APK build

GitHub Actions builds a **debug APK** on every push to `main`:

1. Open **Actions** → workflow **Android Debug APK**
2. Wait for green check
3. Download artifact **web3-browser-debug**

Optional secret: `REOWN_PROJECT_ID` (Settings → Secrets → Actions).  
Details: [docs/CI.md](docs/CI.md)

## Local Android debug

```bash
cd android
cp local.properties.example local.properties
# sdk.dir=... and reown.project.id=...
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Chromium full build

```bash
./scripts/fetch_and_patch_chromium.sh ~/chromium
cd ~/chromium/src && gn args out/Web3Default
autoninja -C out/Web3Default chrome_public_apk
```

## Structure

| Path | Role |
|------|------|
| `android/` | Compose UI, WebView shell, DPI service, wallet |
| `modules/` | provider, resolver, dpi-proxy, ads |
| `patches/` | Chromium integration guides |
| `scripts/` | fetch/patch helpers |
| `.github/workflows/` | CI |

## License

Apache-2.0 (project code). Chromium is BSD-3-Clause.
