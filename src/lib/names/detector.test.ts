import assert from "node:assert/strict";
import { describe, it } from "node:test";
import { classifyHostname, classifyInput } from "./detector.ts";
import { matchProviderId } from "./match.ts";
import { decodeContenthash, contentToGatewayUrl } from "./contenthash.ts";

describe("namespace detector — Web3 before DNS", () => {
  it("routes alice.eth to ENS and never public DNS", () => {
    const c = classifyHostname("alice.eth");
    assert.equal(c.path, "web3");
    assert.equal(c.providerId, "ens");
    assert.match(c.reason, /no public DNS/);
  });

  it("routes ENS subnames to ENS", () => {
    const c = classifyHostname("foo.alice.eth");
    assert.equal(c.path, "web3");
    assert.equal(c.providerId, "ens");
  });

  it("routes ICANN names to web2 after Web3 miss", () => {
    const c = classifyHostname("example.com");
    assert.equal(c.path, "web2");
    assert.equal(c.providerId, null);
  });

  it("classifies naming systems", () => {
    assert.equal(matchProviderId("name.crypto"), "uns");
    assert.equal(matchProviderId("name.sol"), "sns");
    assert.equal(matchProviderId("name.ton"), "ton");
    assert.equal(matchProviderId("name.tez"), "tezos");
    assert.equal(matchProviderId("name.bit"), "namecoin");
    assert.equal(matchProviderId("name.emc"), "emerdns");
  });

  it("Handshake matches unknown non-ICANN TLDs, not .com", () => {
    assert.equal(matchProviderId("example.myhnszone"), "hns");
    assert.equal(matchProviderId("example.com"), null);
  });

  it("search vs hostname", () => {
    const s = classifyInput("what is ens");
    assert.equal(s.path, "search");
    const h = classifyInput("uniswap.eth");
    assert.equal(h.path, "web3");
  });
});

describe("ENS contenthash", () => {
  it("decodes uniswap-style IPNS DNSLink without IP rewrite", () => {
    const hex =
      "0xe5010170000f" +
      Buffer.from("app.uniswap.org", "utf8").toString("hex");
    const decoded = decodeContenthash(hex);
    assert.ok(decoded);
    assert.equal(decoded!.protocol, "ipns");
    assert.equal(decoded!.protocol === "ipns" && decoded!.name, "app.uniswap.org");
    const url = contentToGatewayUrl(decoded!);
    assert.equal(url, "https://dweb.link/ipns/app.uniswap.org");
  });
});
