import { keccak256, toHex } from "./keccak256.ts";

/** ENS namehash (EIP-137). Pure local — no network. */
export function namehash(name: string): Uint8Array {
  let node = new Uint8Array(32);
  if (!name) return node;
  let n = name.trim().toLowerCase();
  if (n.endsWith(".")) n = n.slice(0, -1);
  if (!n) return node;
  const labels = n.split(".");
  for (let i = labels.length - 1; i >= 0; i--) {
    const label = labels[i]!;
    if (!label) continue;
    const labelHash = keccak256(new TextEncoder().encode(label));
    const concat = new Uint8Array(64);
    concat.set(node, 0);
    concat.set(labelHash, 32);
    node = keccak256(concat);
  }
  return node;
}

export function namehashHex(name: string): string {
  return "0x" + toHex(namehash(name));
}
