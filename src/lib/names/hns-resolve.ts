/**
 * Handshake (HNS) resolver via Handshake-aware DoH only.
 * NEVER uses ICANN public DNS (1.1.1.1, 8.8.8.8).
 */
import {
  encodeDnsQuery,
  parseDnsAnswers,
  ipv4ToString,
  ipv6ToString,
  txtToString,
  toBase64Url,
  QTYPE,
} from "./dns-wire.ts";
import type { ResolveResult, ResolveRecord, NavigationTarget } from "./types.ts";

export const HNS_DOH_ENDPOINTS = [
  "https://dns.easyhns.com/dns-query",
  "https://hnsdoh.com/dns-query",
] as const;

export const HNS_GATEWAY = "https://hns.to";

export async function resolveHns(hostname: string): Promise<ResolveResult> {
  const trace: string[] = [];
  const name = hostname.trim().toLowerCase().replace(/\.$/, "");
  trace.push(`canonicalize → ${name}`);
  trace.push("namespace HNS (Handshake)");
  trace.push("provider hns — HNS DoH only, not ICANN public DNS");

  let lastErr: string | null = null;
  for (const endpoint of HNS_DOH_ENDPOINTS) {
    try {
      trace.push(`doh ${endpoint}`);
      const result = await resolveOnEndpoint(name, endpoint, trace);
      if (result) return result;
      trace.push("doh empty answers — try next");
    } catch (e) {
      lastErr = e instanceof Error ? e.message : String(e);
      trace.push(`doh error ${lastErr}`);
    }
  }

  return {
    hostname: name,
    namespace: "web3",
    providerId: "hns",
    providerName: "Handshake",
    verified: false,
    records: [],
    target: null,
    verification: { ok: false, reason: lastErr ?? "no answers" },
    error: lastErr ?? "Handshake DoH returned no usable records",
    trace,
    cached: false,
    resolvedAt: Date.now(),
    ttlMs: 60_000,
  };
}

async function resolveOnEndpoint(
  name: string,
  endpoint: string,
  trace: string[],
): Promise<ResolveResult | null> {
  const records: ResolveRecord[] = [];
  let hasAddress = false;
  let minTtl = 300;

  for (const [label, qtype] of [
    ["A", QTYPE.A],
    ["AAAA", QTYPE.AAAA],
    ["CNAME", QTYPE.CNAME],
    ["TXT", QTYPE.TXT],
  ] as const) {
    try {
      const answers = await dohQuery(endpoint, name, qtype);
      for (const a of answers) {
        minTtl = Math.min(minTtl, a.ttl || 300);
        if (a.type === QTYPE.A) {
          const ip = ipv4ToString(a.data);
          if (ip) {
            records.push({ type: "A", value: ip });
            hasAddress = true;
            trace.push(`A ${ip}`);
          }
        } else if (a.type === QTYPE.AAAA) {
          const ip = ipv6ToString(a.data);
          if (ip) {
            records.push({ type: "AAAA", value: ip });
            hasAddress = true;
            trace.push(`AAAA ${ip}`);
          }
        } else if (a.type === QTYPE.CNAME) {
          records.push({ type: "CNAME", value: "present" });
          hasAddress = true;
          trace.push("CNAME present");
        } else if (a.type === QTYPE.TXT) {
          const text = txtToString(a.data);
          if (text) {
            records.push({ type: "TXT", value: text.slice(0, 200) });
            trace.push(`TXT ${text.slice(0, 40)}${text.length > 40 ? "…" : ""}`);
          }
        }
      }
    } catch (e) {
      trace.push(`${label} skip ${e instanceof Error ? e.message : e}`);
    }
  }

  if (!hasAddress && records.filter((r) => r.type === "TXT").length === 0) {
    return null;
  }

  let target: NavigationTarget | null = null;
  if (hasAddress) {
    const url = `${HNS_GATEWAY}/${name}`;
    target = {
      kind: "https",
      url,
      hostname: name,
      trust: "gateway",
      gateway: HNS_GATEWAY,
    };
    trace.push(`target ${url} trust=gateway`);
  } else {
    trace.push("TXT-only — no navigation target (no A/AAAA/CNAME)");
  }

  return {
    hostname: name,
    namespace: "web3",
    providerId: "hns",
    providerName: "Handshake",
    verified: hasAddress,
    records,
    target,
    verification: {
      ok: hasAddress,
      reason: hasAddress ? "hns-doh records" : "txt only",
    },
    error: hasAddress
      ? null
      : "Handshake name has TXT but no A/AAAA/CNAME — refusing blind gateway",
    trace,
    cached: false,
    resolvedAt: Date.now(),
    ttlMs: Math.max(30_000, minTtl * 1000),
  };
}

async function dohQuery(
  endpoint: string,
  name: string,
  qtype: number,
): Promise<{ type: number; ttl: number; data: Uint8Array }[]> {
  const wire = encodeDnsQuery(name, qtype);
  const b64 = toBase64Url(wire);
  const getUrl = `${endpoint}?dns=${b64}`;
  try {
    const res = await fetch(getUrl, {
      headers: { Accept: "application/dns-message" },
    });
    if (res.ok) {
      const buf = new Uint8Array(await res.arrayBuffer());
      return parseDnsAnswers(buf);
    }
  } catch {
    // POST fallback
  }
  const res = await fetch(endpoint, {
    method: "POST",
    headers: {
      "content-type": "application/dns-message",
      accept: "application/dns-message",
    },
    body: wire,
  });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  const buf = new Uint8Array(await res.arrayBuffer());
  return parseDnsAnswers(buf);
}
