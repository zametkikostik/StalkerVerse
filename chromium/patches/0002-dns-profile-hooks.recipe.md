# Recipe: 0002-dns-profile-hooks

**Pin:** Chromium `154.0.8037.126`  
**Depends on:** `android/java/org/stalkerverse/dns/DnsProfiles.java`  
**Must not:** send Web3 hostnames through these resolvers

## Intent

Wire user-selected encrypted DNS (default AdGuard DoH) into Chromium’s DNS client **only for Web2** navigations.

## Profiles

| ID | Transport | Endpoint |
|----|-----------|----------|
| `adguard-doh` (default) | DoH | `https://dns.adguard-dns.com/dns-query` |
| `adguard-dot` | DoT | `tls://dns.adguard-dns.com` |
| `adguard-doq` | DoQ | `quic://dns.adguard-dns.com` |
| `system` | system | platform default |

## Touch points

1. Prefer Chromium built-in Secure DNS / DoH templates over a custom resolver.
2. Prefs: secure mode + template `https://dns.adguard-dns.com/dns-query`.
3. Settings UI lists `DnsProfiles.ALL`.
4. Hard gate: Web3 names never reach HostResolver via this profile.

## Verification

- `example.com` via AdGuard DoH.
- `uniswap.eth` → zero DNS queries for that name on AdGuard/system.

## Out of scope (V1)

- DPI circumvention; custom DoQ if pin lacks QUIC DNS (fall back to DoH).
