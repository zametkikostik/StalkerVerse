/**
 * ENS resolve over public Ethereum JSON-RPC — mirror of Android EnsResolver.
 * Never uses public DNS for the .eth name.
 */
import { namehash, namehashHex } from "./ens-namehash.ts";
import { toHex } from "./keccak256.ts";
import { decodeContenthash, contentToGatewayUrl } from "./contenthash.ts";
import type { ResolveResult, TrustLabel } from "./types.ts";

export const ENS_REGISTRY = "0x00000000000C2E074eC69A0dFb2997BA6C7d2e1e";
export const ETH_RPCS = [
  "https://ethereum.publicnode.com",
  "https://eth.llamarpc.com",
  "https://cloudflare-eth.com",
] as const;

const SEL_RESOLVER = "0178b8bf";
const SEL_CONTENTHASH = "bc1c58d1";
const SEL_ADDR = "3b3b57de";

function callDataNode(selector: string, node: Uint8Array): string {
  return "0x" + selector + toHex(node);
}

function decodeAddress(result: string): string | null {
  if (!result || result === "0x" || result.length < 42) return null;
  const h = result.startsWith("0x") ? result.slice(2) : result;
  const addr = "0x" + h.slice(-40);
  if (addr === "0x0000000000000000000000000000000000000000") return null;
  return addr;
}

function decodeBytesHex(result: string): string | null {
  if (!result || result === "0x" || result.length < 2 + 64 + 64) return null;
  const h = result.startsWith("0x") ? result.slice(2) : result;
  const length = Number.parseInt(h.slice(64, 128), 16);
  if (!Number.isFinite(length) || length <= 0) return "0x";
  const dataStart = 128;
  const hexLen = length * 2;
  if (h.length < dataStart + hexLen) return null;
  return "0x" + h.slice(dataStart, dataStart + hexLen);
}

async function ethCall(
  endpoint: string,
  to: string,
  data: string,
): Promise<string> {
  const res = await fetch(endpoint, {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify({
      jsonrpc: "2.0",
      id: 1,
      method: "eth_call",
      params: [{ to, data }, "latest"],
    }),
  });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  const json = (await res.json()) as {
    result?: string;
    error?: { message?: string };
  };
  if (json.error) throw new Error(json.error.message ?? "rpc error");
  return json.result ?? "0x";
}

export async function resolveEns(hostname: string): Promise<ResolveResult> {
  const trace: string[] = [];
  const name = hostname.trim().toLowerCase().replace(/\.$/, "");
  trace.push(`canonicalize → ${name}`);
  trace.push("namespace ENS (.eth)");
  trace.push("provider ens — Ethereum RPC, not public DNS");

  const node = namehash(name);
  trace.push(`namehash ${namehashHex(name)}`);

  let lastErr: string | null = null;
  for (const rpc of ETH_RPCS) {
    try {
      trace.push(`rpc ${rpc}`);
      return await resolveOnRpc(name, node, rpc, trace);
    } catch (e) {
      lastErr = e instanceof Error ? e.message : String(e);
      trace.push(`rpc error ${lastErr}`);
    }
  }
  return {
    hostname: name,
    namespace: "web3",
    providerId: "ens",
    providerName: "ENS",
    verified: false,
    records: [],
    target: null,
    verification: { ok: false, reason: lastErr ?? "All RPCs failed" },
    error: lastErr ?? "All Ethereum RPCs failed",
    trace,
    cached: false,
    resolvedAt: Date.now(),
    ttlMs: 60_000,
  };
}

async function resolveOnRpc(
  hostname: string,
  node: Uint8Array,
  rpc: string,
  trace: string[],
): Promise<ResolveResult> {
  const resolverRaw = await ethCall(
    rpc,
    ENS_REGISTRY,
    callDataNode(SEL_RESOLVER, node),
  );
  const resolver = decodeAddress(resolverRaw);
  if (!resolver) {
    trace.push("resolver none");
    return empty(hostname, trace, "No resolver set");
  }
  trace.push(`resolver ${resolver}`);

  let contenthashHex: string | null = null;
  let addr: string | null = null;

  try {
    const chRaw = await ethCall(
      rpc,
      resolver,
      callDataNode(SEL_CONTENTHASH, node),
    );
    contenthashHex = decodeBytesHex(chRaw);
    if (contenthashHex && contenthashHex !== "0x") {
      trace.push(
        `contenthash ${contenthashHex.length > 18 ? contenthashHex.slice(0, 18) + "…" : contenthashHex}`,
      );
    }
  } catch (e) {
    trace.push(`contenthash skip ${e instanceof Error ? e.message : e}`);
  }

  try {
    const addrRaw = await ethCall(
      rpc,
      resolver,
      callDataNode(SEL_ADDR, node),
    );
    addr = decodeAddress(addrRaw);
    if (addr) trace.push(`addr ${addr}`);
  } catch (e) {
    trace.push(`addr skip ${e instanceof Error ? e.message : e}`);
  }

  const records: { type: string; value: string }[] = [];
  if (addr) records.push({ type: "addr", value: addr });
  if (contenthashHex && contenthashHex !== "0x") {
    records.push({ type: "contenthash", value: contenthashHex });
  }

  const decoded = contenthashHex ? decodeContenthash(contenthashHex) : null;
  if (decoded) {
    const url = contentToGatewayUrl(decoded);
    const trust: TrustLabel =
      decoded.protocol === "swarm" ? "direct" : "gateway";
    trace.push(`decoded ${decoded.protocol} trust=${trust}`);
    return {
      hostname,
      namespace: "web3",
      providerId: "ens",
      providerName: "ENS",
      verified: true,
      records,
      target: {
        kind:
          decoded.protocol === "ipns"
            ? "ipns"
            : decoded.protocol === "ipfs"
              ? "ipfs"
              : "https",
        url,
        hostname,
        trust,
      },
      verification: { ok: true, reason: "contenthash" },
      error: null,
      trace,
      cached: false,
      resolvedAt: Date.now(),
      ttlMs: 300_000,
    };
  }

  trace.push("no verified contenthash — refusing IP rewrite");
  return {
    hostname,
    namespace: "web3",
    providerId: "ens",
    providerName: "ENS",
    verified: false,
    records,
    target: null,
    verification: { ok: false, reason: "no contenthash" },
    error:
      "ENS name resolved but has no verified contenthash. Refusing IP rewrite.",
    trace,
    cached: false,
    resolvedAt: Date.now(),
    ttlMs: 60_000,
  };
}

function empty(
  hostname: string,
  trace: string[],
  error: string,
): ResolveResult {
  return {
    hostname,
    namespace: "web3",
    providerId: "ens",
    providerName: "ENS",
    verified: false,
    records: [],
    target: null,
    verification: { ok: false, reason: error },
    error,
    trace,
    cached: false,
    resolvedAt: Date.now(),
    ttlMs: 60_000,
  };
}
