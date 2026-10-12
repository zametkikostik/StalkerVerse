# Roadmap

## Phase 0 — Chromium foundation
- [x] Pin revision `154.0.8037.126`
- [x] `scripts/fetch-chromium.sh`, `apply-overlay.sh`, `build-apk.sh`
- [x] GitHub Actions workflow for large/self-hosted runners
- [x] `scripts/check-runner.sh` + `docs/SELF-HOSTED-APK.md`
- [ ] First green `chrome_public_apk` on runner

## Phase 1 — Browser shell
- [x] Tabs, omnibox, history, bookmarks, settings (web preview)
- [x] i18n EN / RU / BG
- [x] Branding recipe for Android product strings
- [ ] Native Android UI on Chromium shell

## Phase 2 — Web3 names
- [x] Pure `classifyHostname` + provider registry design
- [x] ENS resolve + contenthash — TS + Java
- [x] Navigation interceptor design + Android NavigationGate
- [x] ContenthashDecoder + EnsContenthash (Java)
- [x] Full ENS namehash + eth_call in Java
- [x] HNS production resolver (HNS DoH + hns.to, TS + tests)
- [ ] Native omnibox hook inside Chromium tree
- [ ] End-to-end native test on device/emulator
- [ ] UNS / SNS / TON / Tezos / Namecoin / EmerDNS production resolvers

## Phase 3 — DNS privacy
- [x] AdGuard DoH/DoT/DoQ profiles (TS + Java)
- [x] DNS profile patch recipe `0002`
- [ ] Wire into Chromium DNS client for Web2 only

## Phase 4–8
- DApp bridge, wallet, ads, battery, SBOM — not started
