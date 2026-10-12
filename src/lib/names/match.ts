/**
 * Pure provider matching — no network, no viem.
 * Used by classifier before any DNS or RPC.
 */
import {
  EMERDNS_TLDS,
  ENS_TLDS,
  ICANN_GTLD_LIST,
  NAMECOIN_TLDS,
  SNS_TLDS,
  TEZOS_TLDS,
  TON_TLDS,
  UNS_TLDS,
} from "./namespaces.ts";
import { hostnameTld, isPunycodeTld } from "./canonicalize.ts";
import type { ProviderId } from "./types.ts";

export function isIcannLikeTld(tld: string): boolean {
  if (!tld) return false;
  if (ICANN_GTLD_LIST.has(tld)) return true;
  if (isPunycodeTld(tld)) return true;
  if (/^[a-z]{2}$/.test(tld)) return true;
  return false;
}

export function providerMatches(id: ProviderId, hostname: string): boolean {
  const tld = hostnameTld(hostname);
  switch (id) {
    case "ens":
      return ENS_TLDS.has(tld);
    case "uns":
      return UNS_TLDS.has(tld);
    case "sns":
      return SNS_TLDS.has(tld);
    case "ton":
      return TON_TLDS.has(tld);
    case "tezos":
      return TEZOS_TLDS.has(tld);
    case "namecoin":
      return NAMECOIN_TLDS.has(tld);
    case "emerdns":
      return EMERDNS_TLDS.has(tld);
    case "hns":
      return Boolean(tld) && !isIcannLikeTld(tld) && !isExplicitWeb3Tld(tld);
    default:
      return false;
  }
}

function isExplicitWeb3Tld(tld: string): boolean {
  return (
    ENS_TLDS.has(tld) ||
    UNS_TLDS.has(tld) ||
    SNS_TLDS.has(tld) ||
    TON_TLDS.has(tld) ||
    TEZOS_TLDS.has(tld) ||
    NAMECOIN_TLDS.has(tld) ||
    EMERDNS_TLDS.has(tld)
  );
}

const PROVIDER_ORDER: ProviderId[] = [
  "ens",
  "uns",
  "sns",
  "ton",
  "tezos",
  "namecoin",
  "emerdns",
  "hns",
];

export function matchProviderId(hostname: string): ProviderId | null {
  for (const id of PROVIDER_ORDER) {
    if (providerMatches(id, hostname)) return id;
  }
  return null;
}
