/** Top-level resolve: classify first, then provider. Never DNS for Web3 path. */
import { classifyHostname } from "./detector.ts";
import { resolveEns } from "./ens-resolve.ts";
import { resolveHns } from "./hns-resolve.ts";
import {
  resolveEmerdnsStub,
  resolveNamecoinStub,
  resolveSnsStub,
  resolveTezosStub,
  resolveTonStub,
  resolveUnsStub,
} from "./providers-stub.ts";
import type { Classification, ResolveResult } from "./types.ts";

export async function resolveHostname(hostname: string): Promise<{
  classification: Classification;
  result: ResolveResult | null;
  web2: boolean;
}> {
  const classification = classifyHostname(hostname);
  if (classification.path !== "web3" || !classification.hostname) {
    return {
      classification,
      result: null,
      web2: classification.path === "web2",
    };
  }

  const host = classification.hostname;
  const id = classification.providerId;

  let result: ResolveResult;
  switch (id) {
    case "ens":
      result = await resolveEns(host);
      break;
    case "hns":
      result = await resolveHns(host);
      break;
    case "uns":
      result = resolveUnsStub(host);
      break;
    case "sns":
      result = resolveSnsStub(host);
      break;
    case "ton":
      result = resolveTonStub(host);
      break;
    case "tezos":
      result = resolveTezosStub(host);
      break;
    case "namecoin":
      result = resolveNamecoinStub(host);
      break;
    case "emerdns":
      result = resolveEmerdnsStub(host);
      break;
    default:
      result = {
        hostname: host,
        namespace: "web3",
        providerId: id ?? "unknown",
        providerName: "unknown",
        verified: false,
        records: [],
        target: null,
        verification: { ok: false, reason: "unknown provider" },
        error: "unknown provider",
        trace: [],
        cached: false,
        resolvedAt: Date.now(),
        ttlMs: 0,
      };
  }

  return { classification, result, web2: false };
}
