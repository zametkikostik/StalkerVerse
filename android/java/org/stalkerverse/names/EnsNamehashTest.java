package org.stalkerverse.names;

/** ENS namehash vectors from EIP-137 / ENS docs. */
public final class EnsNamehashTest {
  private EnsNamehashTest() {}

  private static final String EMPTY =
      "0x0000000000000000000000000000000000000000000000000000000000000000";
  private static final String ETH =
      "0x93cdeb708b7545dc668eb9280176169d1c33cfd8ed6f04690a0bcc88a93fc4ae";
  private static final String FOO_ETH =
      "0xde9b09fd7c5f901e23a3f19fecc54828e9c848539801e86591bd9801b019f84f";
  private static final String ALICE_ETH =
      "0x787192fc5378cc32aa956ddfdedbf26b24e8d78e40109add0eea2c1a012c3dec";
  private static final String UNISWAP_ETH =
      "0xec9ec573cf97ad1c270be71ac1de3b382790cb346036130c7d7ff844bf8f4974";
  private static final String LABEL_ETH =
      "4f5b812789fc606be1b3b16908db13fc7a9adf7ca72641f84d75b47069d3d7f0";

  public static void assertInvariants() {
    require(EMPTY.equals(EnsNamehash.namehashHex("")), "empty");
    require(ETH.equals(EnsNamehash.namehashHex("eth")), "eth");
    require(FOO_ETH.equals(EnsNamehash.namehashHex("foo.eth")), "foo.eth");
    require(ALICE_ETH.equals(EnsNamehash.namehashHex("alice.eth")), "alice.eth");
    require(UNISWAP_ETH.equals(EnsNamehash.namehashHex("uniswap.eth")), "uniswap.eth");
    require(ETH.equals(EnsNamehash.namehashHex("ETH")), "case fold");
    require(ETH.equals(EnsNamehash.namehashHex("eth.")), "trailing dot");
    String label =
        Keccak256.toHex(Keccak256.hash("eth".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    require(LABEL_ETH.equals(label), "labelhash eth");
  }

  private static void require(boolean cond, String msg) {
    if (!cond) throw new IllegalStateException("Invariant failed: " + msg);
  }

  public static void main(String[] args) {
    assertInvariants();
    System.out.println("EnsNamehash + Keccak256 vectors OK");
  }
}
