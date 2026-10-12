/**
 * Provider stubs for namespaces beyond ENS.
 * Never fall back to public DNS for these hostnames.
 */
import type { ProviderId, ResolveResult } from "./types.ts";

export function unresolvedProvider(
  hostname: string,
  providerId: ProviderId,
  displayName: string,
): ResolveResult {
  return {
    hostname,
    namespace: "web3",
    providerId,
    providerName: displayName,
    verified: false,
    records: [],
    target: null,
    verification: { ok: false, reason: "provider not wired" },
    error: `${displayName} resolver not implemented yet — no public DNS fallback for ${hostname}`,
    trace: [
      `provider ${providerId}`,
      "no public DNS for this namespace",
      "status: stub",
    ],
    cached: false,
    resolvedAt: Date.now(),
    ttlMs: 60_000,
  };
}

export function resolveHnsStub(hostname: string): ResolveResult {
  return unresolvedProvider(hostname, "hns", "Handshake");
}
export function resolveUnsStub(hostname: string): ResolveResult {
  return unresolvedProvider(hostname, "uns", "Unstoppable Domains");
}
export function resolveSnsStub(hostname: string): ResolveResult {
  return unresolvedProvider(hostname, "sns", "Solana Name Service");
}
export function resolveTonStub(hostname: string): ResolveResult {
  return unresolvedProvider(hostname, "ton", "TON DNS");
}
export function resolveTezosStub(hostname: string): ResolveResult {
  return unresolvedProvider(hostname, "tezos", "Tezos Domains");
}
export function resolveNamecoinStub(hostname: string): ResolveResult {
  return unresolvedProvider(hostname, "namecoin", "Namecoin");
}
export function resolveEmerdnsStub(hostname: string): ResolveResult {
  return unresolvedProvider(hostname, "emerdns", "EmerDNS");
}
