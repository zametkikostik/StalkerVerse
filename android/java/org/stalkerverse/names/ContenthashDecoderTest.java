package org.stalkerverse.names;

/** Mirrors src/lib/names contenthash uniswap case. */
public final class ContenthashDecoderTest {
  private ContenthashDecoderTest() {}

  public static void assertInvariants() {
    String hex =
        "0xe5010170000f"
            + toHex("app.uniswap.org".getBytes(java.nio.charset.StandardCharsets.UTF_8));
    ContenthashDecoder.Decoded d = ContenthashDecoder.decode(hex);
    require(d != null, "decoded non-null");
    require(d.protocol == ContenthashDecoder.Protocol.IPNS, "ipns");
    require("app.uniswap.org".equals(d.value), "name app.uniswap.org");
    String url = ContenthashDecoder.toGatewayUrl(d, "https://dweb.link");
    require("https://dweb.link/ipns/app.uniswap.org".equals(url), "gateway url");
    require(ContenthashDecoder.trustFor(d) == ResolveResult.Trust.GATEWAY, "gateway trust");
    require(ContenthashDecoder.decode("0x") == null, "empty 0x");
    require(ContenthashDecoder.decode(null) == null, "null");
  }

  private static String toHex(byte[] b) {
    StringBuilder sb = new StringBuilder();
    for (byte value : b) sb.append(String.format("%02x", value & 0xff));
    return sb.toString();
  }

  private static void require(boolean cond, String msg) {
    if (!cond) throw new IllegalStateException("Invariant failed: " + msg);
  }

  public static void main(String[] args) {
    assertInvariants();
    System.out.println("ContenthashDecoder invariants OK");
  }
}
