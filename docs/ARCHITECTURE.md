# Architecture

Android UI (Compose) → Chromium Browser Process → Renderer / Network / Services

## Modules
- Web3: ethereum_provider.js injection + Web3Bridge
- Domains: ENS/UD resolver
- DPI: local proxy + DpiProxyService
- Wallet: HD (Keystore) + WalletConnect
- Ads: on-device InterestEngine

See modules/ and android/ for implementation.
