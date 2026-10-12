import { canonicalizeHostname } from "./canonicalize.ts";
import { isIcannLikeTld, matchProviderId } from "./match.ts";
import { hostnameTld } from "./canonicalize.ts";
import type { Classification } from "./types.ts";

/**
 * Classify omnibox input BEFORE any network call.
 * Web3 path must never fall through to public DNS.
 */
export function classifyHostname(hostname: string): Classification {
  const host = canonicalizeHostname(hostname);
  if (!host) {
    return { path: "invalid", hostname: null, providerId: null, reason: "invalid hostname" };
  }
  const providerId = matchProviderId(host);
  if (providerId) {
    return {
      path: "web3",
      hostname: host,
      providerId,
      reason: `web3 provider ${providerId} — no public DNS`,
    };
  }
  const tld = hostnameTld(host);
  if (isIcannLikeTld(tld) || host.includes(".")) {
    return {
      path: "web2",
      hostname: host,
      providerId: null,
      reason: "icann-like / web2 DNS path",
    };
  }
  return {
    path: "web2",
    hostname: host,
    providerId: null,
    reason: "default web2",
  };
}

export function classifyInput(raw: string): Classification {
  const value = raw.trim();
  if (!value) {
    return { path: "invalid", hostname: null, providerId: null, reason: "empty" };
  }
  if (value.includes("://") || value.includes(".") || value.endsWith(".eth")) {
    try {
      const host = value.includes("://")
        ? new URL(value).hostname
        : value.replace(/\/.*$/, "");
      return classifyHostname(host);
    } catch {
      return classifyHostname(value);
    }
  }
  if (/\s/.test(value) || !value.includes(".")) {
    return {
      path: "search",
      hostname: null,
      providerId: null,
      searchQuery: value,
      reason: "search query",
    };
  }
  return classifyHostname(value);
}
