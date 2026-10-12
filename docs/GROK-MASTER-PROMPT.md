# Grok Master Prompt (project constraints)

Use this when continuing StalkerVerse work with an agent.

## Product

**Web3 StalkerVerse** — privacy-first Chromium browser with native Web3 naming, DApp support, optional local wallet, encrypted DNS, multilingual UI, battery-aware design, privacy-preserving optional ads.

## Build order

1. Phase 0/1: real Chromium pin + buildable APK path before expanding Web3.
2. Never ship a WebView pretending to be Chromium.
3. Never invent “resolved” content by rewriting bare IPs for Web3 names.

## Forbidden

- Toy browser / mocks that claim success without network resolve
- Vendor Chromium source tree into git
- Private keys to servers
- DPI bypass as a product feature in V1 (network privacy only)
- Brave Core service dependency

## Required behavior

- `classifyHostname` pure and local
- `.eth` never hits public DNS
- Gateway trust shown explicitly
- License SBOM separate from Brave

## Verification

- Unit tests: namespace collisions
- Live ENS: `uniswap.eth` → contenthash/IPNS with gateway banner
- Web2: `example.com` still works via encrypted DNS path
