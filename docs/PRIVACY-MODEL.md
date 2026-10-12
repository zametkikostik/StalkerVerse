# Privacy model

## Guarantees

1. **No telemetry** from StalkerVerse layer.
2. **Wallet keys** stay in Android Keystore / encrypted vault.
3. **Web3 names** resolve via provider RPCs only — not via public DNS.
4. **Web2 DNS** uses user-selected encrypted profile (default AdGuard DoH).
5. **Gateway loads** are labeled; user sees that trust is not identical to trustless retrieval.
6. **Ads** (if ever enabled) are contextual, local preference based, stablecoin escrow — off by default.

## Explicit non-goals (V1)

- DPI / GFW circumvention tooling
- Full Bitcoin/Ethereum nodes on device
- Cloud wallet recovery / custodial keys
- Silent background chain sync

## Network privacy (allowed)

- DNS-over-HTTPS / TLS / QUIC
- Encrypted Client Hello (ECH) where supported
- Tracker / fingerprinting resistance via Chromium prefs + overlay
- Partitioned storage / strict site isolation

## Endpoint inventory (document in app)

All outbound endpoints used for Web3 resolve and DNS must be listed in Settings → Privacy → Network endpoints, with role and whether they are Web3-only.
