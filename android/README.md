# Android module (Compose UI + Bridge)

Каркас нативного Android-слоя для Web3 Chromium Browser.

## Contents

- `bridge/Web3Bridge.kt` — JavascriptInterface for ethereum_provider.js
- `bridge/MockWalletController.kt` — in-memory wallet for development
- `wallet/HdWalletController.kt` — BIP-39 HD wallet (web3j + Keystore)
- `wallet/WalletConnectController.kt` — Reown WalletConnect v2
- `dpi/DpiProxyService.kt` — foreground local CONNECT proxy
- `ui/` — WalletScreen, SettingsScreen, AdSurface
- `MainActivity.kt` — WebView debug shell

## Build

```bash
cp local.properties.example local.properties
# set sdk.dir and reown.project.id
./gradlew :app:assembleDebug
```

See `docs/BUILD_ANDROID.md`.
