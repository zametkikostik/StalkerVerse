/**
 * Keccak-256 (Ethereum / ENS, not NIST SHA-3).
 * Same algorithm as android/.../Keccak256.java — verified against ENS docs vectors.
 */

const RC = [
  0x0000000000000001n, 0x0000000000008082n, 0x800000000000808an, 0x8000000080008000n,
  0x000000000000808bn, 0x0000000080000001n, 0x8000000080008081n, 0x8000000000008009n,
  0x000000000000008an, 0x0000000000000088n, 0x0000000080008009n, 0x000000008000000an,
  0x000000008000808bn, 0x800000000000008bn, 0x8000000000008089n, 0x8000000000008003n,
  0x8000000000008002n, 0x8000000000000080n, 0x000000000000800an, 0x800000008000000an,
  0x8000000080008081n, 0x8000000000008080n, 0x0000000080000001n, 0x8000000080008008n,
];

const ROT = [
  [0, 36, 3, 41, 18],
  [1, 44, 10, 45, 2],
  [62, 6, 43, 15, 61],
  [28, 55, 25, 21, 56],
  [27, 20, 39, 8, 14],
];

function rotl(x: bigint, n: number): bigint {
  const mask = (1n << 64n) - 1n;
  n %= 64;
  return ((x << BigInt(n)) | (x >> BigInt(64 - n))) & mask;
}

function keccakF(st: bigint[]): void {
  const c = new Array<bigint>(5);
  const d = new Array<bigint>(5);
  const b = new Array<bigint>(25);
  const mask = (1n << 64n) - 1n;
  for (let round = 0; round < 24; round++) {
    for (let x = 0; x < 5; x++) {
      c[x] = st[x]! ^ st[x + 5]! ^ st[x + 10]! ^ st[x + 15]! ^ st[x + 20]!;
    }
    for (let x = 0; x < 5; x++) {
      d[x] = c[(x + 4) % 5]! ^ rotl(c[(x + 1) % 5]!, 1);
    }
    for (let i = 0; i < 25; i++) st[i] = (st[i]! ^ d[i % 5]!) & mask;
    for (let x = 0; x < 5; x++) {
      for (let y = 0; y < 5; y++) {
        b[y + ((2 * x + 3 * y) % 5) * 5] = rotl(st[x + 5 * y]!, ROT[x]![y]!);
      }
    }
    for (let x = 0; x < 5; x++) {
      for (let y = 0; y < 5; y++) {
        st[x + 5 * y] =
          (b[x + 5 * y]! ^
            (~b[((x + 1) % 5) + 5 * y]! & b[((x + 2) % 5) + 5 * y]!)) &
          mask;
      }
    }
    st[0] = (st[0]! ^ RC[round]!) & mask;
  }
}

export function keccak256(input: Uint8Array): Uint8Array {
  const st = new Array<bigint>(25).fill(0n);
  const rate = 136;
  const pad = new Uint8Array(input.length + rate);
  pad.set(input);
  let len = input.length;
  pad[len++] = 0x01;
  while (len % rate !== 0) pad[len++] = 0x00;
  pad[len - 1]! |= 0x80;
  for (let i = 0; i < len; i += rate) {
    for (let j = 0; j < rate; j += 8) {
      let v = 0n;
      for (let k = 0; k < 8; k++) {
        v |= BigInt(pad[i + j + k]!) << BigInt(8 * k);
      }
      st[j / 8] = (st[j / 8]! ^ v) & ((1n << 64n) - 1n);
    }
    keccakF(st);
  }
  const out = new Uint8Array(32);
  for (let i = 0; i < 4; i++) {
    let v = st[i]!;
    for (let j = 0; j < 8; j++) {
      out[i * 8 + j] = Number(v & 0xffn);
      v >>= 8n;
    }
  }
  return out;
}

export function toHex(bytes: Uint8Array): string {
  return [...bytes].map((b) => b.toString(16).padStart(2, "0")).join("");
}

export function keccak256Hex(input: Uint8Array | string): string {
  const bytes =
    typeof input === "string" ? new TextEncoder().encode(input) : input;
  return toHex(keccak256(bytes));
}
