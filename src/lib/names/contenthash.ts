/**
 * EIP-1577 contenthash decoder.
 * Never treats a raw IP as a navigation target.
 */

export type DecodedContent =
  | { protocol: "ipfs"; cid: string }
  | { protocol: "ipns"; name: string }
  | { protocol: "swarm"; reference: string }
  | { protocol: "arweave"; txId: string };

function hexToBytes(hex: string): Uint8Array {
  const h = hex.startsWith("0x") ? hex.slice(2) : hex;
  if (h.length % 2 !== 0) throw new Error("odd contenthash");
  const out = new Uint8Array(h.length / 2);
  for (let i = 0; i < out.length; i++) {
    out[i] = Number.parseInt(h.slice(i * 2, i * 2 + 2), 16);
  }
  return out;
}

function bytesToHex(bytes: Uint8Array): string {
  return [...bytes].map((b) => b.toString(16).padStart(2, "0")).join("");
}

export function decodeContenthash(hex: string): DecodedContent | null {
  if (!hex || hex === "0x" || hex === "0x00") return null;
  let bytes: Uint8Array;
  try {
    bytes = hexToBytes(hex);
  } catch {
    return null;
  }
  if (bytes.length < 2) return null;

  if (
    bytes.length >= 36 &&
    bytes[0] === 0x90 &&
    bytes[1] === 0xb2 &&
    bytes[2] === 0xc6 &&
    bytes[3] === 0x1b
  ) {
    return { protocol: "arweave", txId: bytesToHex(bytes.slice(4, 36)) };
  }

  const ns = bytes[0];
  const rest = bytes.slice(1);

  if (ns === 0xe3) {
    return { protocol: "ipfs", cid: decodeIpfsLike(rest) };
  }
  if (ns === 0xe5) {
    const name = decodeIpnsName(rest);
    return name ? { protocol: "ipns", name } : null;
  }
  if (ns === 0xe4) {
    return { protocol: "swarm", reference: bytesToHex(rest) };
  }
  return null;
}

function decodeIpfsLike(rest: Uint8Array): string {
  return "bafy" + bytesToHex(rest).slice(0, 58);
}

function decodeIpnsName(rest: Uint8Array): string | null {
  let i = 0;
  if (rest[0] === 0x01 && rest[1] === 0x01) i = 1;
  if (rest[i] !== 0x01) {
    return tryIdentityDns(rest);
  }
  i += 1;
  if (rest[i] === 0x70) i += 1;
  else i += 1;
  if (rest[i] === 0x00) {
    const len = rest[i + 1];
    if (len && i + 2 + len <= rest.length) {
      const digest = rest.slice(i + 2, i + 2 + len);
      const text = new TextDecoder().decode(digest);
      if (/^[a-z0-9.-]+$/i.test(text) && text.includes(".")) return text;
    }
  }
  return tryIdentityDns(rest);
}

function tryIdentityDns(rest: Uint8Array): string | null {
  try {
    const text = new TextDecoder().decode(rest);
    const m = text.match(/[a-z0-9][a-z0-9.-]+\.[a-z]{2,}/i);
    return m ? m[0]! : null;
  } catch {
    return null;
  }
}

export function contentToGatewayUrl(
  content: DecodedContent,
  gatewayOrigin = "https://dweb.link",
): string {
  if (content.protocol === "ipfs") return `${gatewayOrigin}/ipfs/${content.cid}`;
  if (content.protocol === "ipns") return `${gatewayOrigin}/ipns/${content.name}`;
  if (content.protocol === "arweave") return `https://arweave.net/${content.txId}`;
  return `bzz://${content.reference}`;
}
