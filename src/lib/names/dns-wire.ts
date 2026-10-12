/**
 * Minimal DNS wire encode/decode for DoH (RFC 1035 + RFC 8484).
 * Used only against Handshake-aware resolvers — never ICANN public DNS.
 */

export const QTYPE = {
  A: 1,
  NS: 2,
  CNAME: 5,
  TXT: 16,
  AAAA: 28,
} as const;

export type QTypeName = keyof typeof QTYPE;

export function encodeDnsQuery(name: string, qtype: number, id = 0xabcd): Uint8Array {
  const labels = name
    .trim()
    .toLowerCase()
    .replace(/\.$/, "")
    .split(".")
    .filter(Boolean);
  const parts: number[] = [];
  parts.push((id >> 8) & 0xff, id & 0xff, 0x01, 0x00, 0x00, 0x01, 0, 0, 0, 0, 0, 0);
  for (const lab of labels) {
    const b = new TextEncoder().encode(lab);
    if (b.length > 63) throw new Error("label too long");
    parts.push(b.length, ...b);
  }
  parts.push(0);
  parts.push((qtype >> 8) & 0xff, qtype & 0xff, 0x00, 0x01);
  return new Uint8Array(parts);
}

export interface DnsAnswer {
  type: number;
  ttl: number;
  data: Uint8Array;
}

export function parseDnsAnswers(msg: Uint8Array): DnsAnswer[] {
  if (msg.length < 12) return [];
  const qd = (msg[4]! << 8) | msg[5]!;
  const an = (msg[6]! << 8) | msg[7]!;
  let i = 12;
  const skipName = (): void => {
    while (i < msg.length) {
      const len = msg[i]!;
      if (len === 0) {
        i += 1;
        return;
      }
      if ((len & 0xc0) === 0xc0) {
        i += 2;
        return;
      }
      i += 1 + len;
    }
  };
  for (let q = 0; q < qd; q++) {
    skipName();
    i += 4;
  }
  const out: DnsAnswer[] = [];
  for (let a = 0; a < an; a++) {
    if (i >= msg.length) break;
    skipName();
    if (i + 10 > msg.length) break;
    const type = (msg[i]! << 8) | msg[i + 1]!;
    i += 4;
    const ttl =
      ((msg[i]! << 24) | (msg[i + 1]! << 16) | (msg[i + 2]! << 8) | msg[i + 3]!) >>> 0;
    i += 4;
    const rdlen = (msg[i]! << 8) | msg[i + 1]!;
    i += 2;
    if (i + rdlen > msg.length) break;
    out.push({ type, ttl, data: msg.slice(i, i + rdlen) });
    i += rdlen;
  }
  return out;
}

export function ipv4ToString(data: Uint8Array): string | null {
  if (data.length !== 4) return null;
  return `${data[0]}.${data[1]}.${data[2]}.${data[3]}`;
}

export function ipv6ToString(data: Uint8Array): string | null {
  if (data.length !== 16) return null;
  const parts: string[] = [];
  for (let i = 0; i < 16; i += 2) {
    parts.push(((data[i]! << 8) | data[i + 1]!).toString(16));
  }
  return parts.join(":");
}

export function txtToString(data: Uint8Array): string {
  const chunks: string[] = [];
  let i = 0;
  while (i < data.length) {
    const len = data[i]!;
    i += 1;
    if (i + len > data.length) break;
    chunks.push(new TextDecoder().decode(data.slice(i, i + len)));
    i += len;
  }
  return chunks.join("") || new TextDecoder().decode(data);
}

export function toBase64Url(buf: Uint8Array): string {
  let s = "";
  for (const b of buf) s += String.fromCharCode(b);
  const b64 =
    typeof btoa !== "undefined"
      ? btoa(s)
      : Buffer.from(buf).toString("base64");
  return b64.replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}
