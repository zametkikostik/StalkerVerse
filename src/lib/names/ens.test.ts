import assert from "node:assert/strict";
import { describe, it } from "node:test";
import { keccak256Hex } from "./keccak256.ts";
import { namehashHex } from "./ens-namehash.ts";
import { resolveEns } from "./ens-resolve.ts";

describe("keccak256 + ENS namehash vectors", () => {
  it("labelhash eth", () => {
    assert.equal(
      keccak256Hex("eth"),
      "4f5b812789fc606be1b3b16908db13fc7a9adf7ca72641f84d75b47069d3d7f0",
    );
  });

  it("namehash empty / eth / foo.eth / alice.eth / uniswap.eth", () => {
    assert.equal(
      namehashHex(""),
      "0x0000000000000000000000000000000000000000000000000000000000000000",
    );
    assert.equal(
      namehashHex("eth"),
      "0x93cdeb708b7545dc668eb9280176169d1c33cfd8ed6f04690a0bcc88a93fc4ae",
    );
    assert.equal(
      namehashHex("foo.eth"),
      "0xde9b09fd7c5f901e23a3f19fecc54828e9c848539801e86591bd9801b019f84f",
    );
    assert.equal(
      namehashHex("alice.eth"),
      "0x787192fc5378cc32aa956ddfdedbf26b24e8d78e40109add0eea2c1a012c3dec",
    );
    assert.equal(
      namehashHex("uniswap.eth"),
      "0xec9ec573cf97ad1c270be71ac1de3b382790cb346036130c7d7ff844bf8f4974",
    );
    assert.equal(namehashHex("ETH"), namehashHex("eth"));
    assert.equal(namehashHex("uniswap.eth."), namehashHex("uniswap.eth"));
  });
});

describe("live ENS resolve (public RPC)", () => {
  it("uniswap.eth → gateway contenthash, never public DNS", async () => {
    const r = await resolveEns("uniswap.eth");
    assert.equal(r.providerId, "ens");
    assert.ok(r.trace.some((t) => t.includes("namehash")));
    assert.ok(r.trace.some((t) => t.includes("not public DNS")));
    if (r.error && !r.target) {
      console.warn("ENS live resolve soft-fail:", r.error, r.trace.slice(-3));
      assert.ok(r.trace.some((t) => t.startsWith("rpc ")));
      return;
    }
    assert.ok(r.target, "expected navigable target");
    assert.equal(r.target!.trust, "gateway");
    assert.match(r.target!.url, /dweb\.link\/ipns\/app\.uniswap\.org/);
    assert.equal(r.verified, true);
  });
});
