# Architecture — Web3 StalkerVerse

## Top-level

```
                    WEB3 STALKERVERSE
                           │
      ┌────────────────────┼────────────────────┐
      │                    │                    │
   Browser              Privacy              Web3
      │                    │                    │
 Tabs Omnibox History   DNS TLS Anti-track   ENS HNS UNS …
                                      │
                               DApp Bridge
                                      │
                               Local Wallet Vault
                                      │
                               Contextual Ads (opt-in)
```

## Chromium base (not Brave)

| Component | Role |
|-----------|------|
| Blink | Layout / rendering |
| V8 | JS |
| Network Service | HTTP/3, TLS, sockets |
| Android Browser UI | Shell to replace / extend |
| Security / Sandbox | Process isolation |

StalkerVerse is an **overlay**: patches + Kotlin/Java UI + native modules.  
We do not vendor Brave Core services.

## StalkerVerse Layer modules

### 1. Web3 Domain Engine
- Provider Adapter Registry (`Web3NameProvider`)
- Namespace detector (pure, local, no network)
- Contenthash decoder (EIP-1577)
- Navigation target builder with trust label: `direct` | `gateway` | `dns`

### 2. DNS Privacy Engine
- Profiles: AdGuard DoH / DoT / DoQ
- Applied **only** to Web2 path after Web3 miss
- Never used for `.eth`, Handshake non-ICANN, etc.

### 3. DApp Bridge
- EIP-1193 provider injection
- EIP-6963 discovery
- No hosted wallet backend

### 4. Wallet Vault
- BIP39 mnemonic generated on device
- Android Keystore encryption
- PIN / biometric unlock
- Keys never uploaded

### 5. Privacy Controls
- Anti-fingerprinting preferences
- ECH / QUIC prefer
- Partitioned storage
- Zero first-party analytics

### 6. Battery Optimizer
- No full blockchain nodes
- RPC TTL cache + batching
- Tab freeze for background renderers
- No background polling loops

### 7. Multilingual UI
- EN / RU / BG (extensible)

## Process model (Android)

```
Browser process  →  Omnibox + classifier + vault UI
Renderer processes →  Sandboxed pages
GPU / Network      →  Chromium services
StalkerVerse RPC   →  Short-lived ENS/HNS calls, then cache
```

## Trust labels (honest UX)

| Label | Meaning |
|-------|---------|
| `direct` | Content addressed without third-party gateway rewrite |
| `gateway` | IPFS/IPNS/Arweave via public gateway — shown in banner |
| `dns` | Classic HTTPS after encrypted DNS |

Never rewrite a bare IP as “the website” for a Web3 name that only has an address record.
