# Domain resolution

## Pipeline (strict order)

```
1. canonicalize input
2. classifyHostname  (pure, local, no network)
3. if web3 → provider.resolve(RPC / dedicated API)
4. if web2 → encrypted DNS (AdGuard profile) or system DNS
5. build NavigationTarget + trust label
6. load in renderer (or show honest “no content target”)
```

## Namespace priority

1. Explicit providers by TLD match (ENS, UNS, SNS, TON, Tezos, Namecoin, EmerDNS)
2. Handshake-style unknown non-ICANN TLD (after ICANN gTLD exclusion list)
3. ICANN-like → Web2

### Supported providers (V1+)

| ID | TLD examples | Backend |
|----|--------------|---------|
| ens | `.eth` | Ethereum JSON-RPC + ENS contracts |
| hns | non-ICANN | Handshake resolvers |
| uns | `.crypto` `.nft` `.x` `.go` `.hi` | UNS/UD |
| sns | `.sol` | Solana Name Service |
| ton | `.ton` | TON DNS |
| tezos | `.tez` | Tezos Domains |
| namecoin | `.bit` | Namecoin |
| emerdns | `.emc` `.lib` `.bazar` | Emercoin |

## ENS contenthash

- Decode EIP-1577 (`0xe3` IPFS, `0xe5` IPNS, Arweave ns, Swarm)
- Identity multihash IPNS may encode a DNS name (e.g. `app.uniswap.org`)
- Prefer contenthash over bare `addr` for navigation
- If only `addr` exists → show records, **refuse IP rewrite**
- Gateway URL must display trust=`gateway`

## Anti-collision

- `.eth` is never queried on public DNS
- Handshake does not claim ICANN gTLDs (`.com`, `.app`, …)
- Tests: `alice.eth` → ENS; `example.com` → web2; unknown TLD → HNS path

## Reference TypeScript

See `src/lib/names/` for the portable classifier and contenthash decoder used in the web preview and portable to Kotlin/C++.
