import assert from "node:assert/strict";
import { describe, it } from "node:test";
import { classifyHostname } from "./detector.ts";
import { resolveHns } from "./hns-resolve.ts";
import { encodeDnsQuery, QTYPE } from "./dns-wire.ts";

describe("HNS classification", () => {
  it("routes unknown non-ICANN TLD to hns", () => {
    const c2 = classifyHostname("nathan.woodburn");
    assert.equal(c2.path, "web3");
    assert.equal(c2.providerId, "hns");
  });

  it("never classifies .com as hns", () => {
    const c = classifyHostname("example.com");
    assert.equal(c.path, "web2");
    assert.notEqual(c.providerId, "hns");
  });
});

describe("dns-wire", () => {
  it("encodes query with question section", () => {
    const q = encodeDnsQuery("nathan.woodburn", QTYPE.A);
    assert.ok(q.length > 12);
    assert.equal(q[2], 0x01);
  });
});

describe("live HNS DoH resolve", () => {
  it("nathan.woodburn → A record + gateway target, not ICANN DNS", async () => {
    const r = await resolveHns("nathan.woodburn");
    assert.equal(r.providerId, "hns");
    assert.ok(r.trace.some((t) => t.includes("not ICANN")));
    if (r.error && !r.target) {
      console.warn("HNS soft-fail:", r.error, r.trace.slice(-4));
      assert.ok(r.trace.some((t) => t.startsWith("doh ")));
      return;
    }
    assert.ok(r.records.some((x) => x.type === "A"));
    assert.ok(r.target);
    assert.equal(r.target!.trust, "gateway");
    assert.match(r.target!.url, /hns\.to\/nathan\.woodburn/);
    assert.equal(r.verified, true);
  });

  it("namebase → A or soft network fail", async () => {
    const r = await resolveHns("namebase");
    assert.equal(r.providerId, "hns");
    if (r.target) {
      assert.match(r.target.url, /hns\.to\/namebase/);
    }
  });
});
