# Roadmap

## Phase 0 — Chromium foundation
- [x] Pin revision `154.0.8037.126`
- [x] `scripts/fetch-chromium.sh`, `apply-overlay.sh`, `build-apk.sh`
- [x] GitHub Actions workflow for large/self-hosted runners
- [ ] First green `chrome_public_apk` on runner

## Phase 1 — Browser shell
- [x] Tabs, omnibox, history, bookmarks, settings (web preview)
- [x] i18n EN / RU / BG
- [ ] Native Android UI on Chromium shell

## Phase 2 — Web3 names
- [x] Pure `classifyHostname` + provider registry design
- [x] ENS resolve + contenthash (IPNS identity DNSLink)
- [ ] HNS / UNS / SNS / TON / Tezos / Namecoin / EmerDNS production resolvers
- [ ] Native NetworkService hook: intercept navigation before DNS

## Phase 3 — DNS privacy
- [x] AdGuard DoH/DoT/DoQ profile constants
- [ ] Wire into Chromium DNS client for Web2 only

## Phase 4 — DApp bridge
- [ ] EIP-1193 injection
- [ ] EIP-6963

## Phase 5 — Wallet vault
- [ ] BIP39 + Keystore
- [ ] Sign typed data / transactions with user confirm

## Phase 6 — Ads (optional)
- [ ] Local contextual only
- [ ] Stablecoin escrow model
- [ ] Default OFF

## Phase 7 — Battery
- [ ] Renderer freeze, RPC cache TTL, no background poll

## Phase 8 — Release
- [ ] SBOM, privacy audit checklist, signed APK pipeline
