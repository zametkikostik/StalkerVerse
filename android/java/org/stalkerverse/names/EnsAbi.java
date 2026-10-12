package org.stalkerverse.names;

/** Minimal ABI encoding for ENS registry + public resolver eth_call. */
public final class EnsAbi {
  public static final String REGISTRY = "0x00000000000C2E074eC69A0dFb2997BA6C7d2e1e";

  public static final String SEL_RESOLVER = "0178b8bf";
  public static final String SEL_CONTENTHASH = "bc1c58d1";
  public static final String SEL_ADDR = "3b3b57de";
  public static final String SEL_TEXT = "59d1d43c";

  private EnsAbi() {}

  public static String callDataNode(String selectorHex, byte[] node) {
    if (node.length != 32) throw new IllegalArgumentException("node must be 32 bytes");
    return "0x" + selectorHex + Keccak256.toHex(node);
  }

  public static String callDataText(byte[] node, String key) {
    byte[] keyBytes = key.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    StringBuilder sb = new StringBuilder();
    sb.append("0x").append(SEL_TEXT);
    sb.append(Keccak256.toHex(node));
    sb.append(padUint(64));
    sb.append(padUint(keyBytes.length));
    sb.append(padBytes(keyBytes));
    return sb.toString();
  }

  static String padUint(long v) {
    return String.format("%064x", v);
  }

  static String padBytes(byte[] data) {
    String hex = Keccak256.toHex(data);
    int pad = (32 - (data.length % 32)) % 32;
    StringBuilder sb = new StringBuilder(hex);
    for (int i = 0; i < pad; i++) sb.append("00");
    return sb.toString();
  }

  public static String decodeAddress(String resultHex) {
    if (resultHex == null || resultHex.equals("0x") || resultHex.length() < 42) return null;
    String h = resultHex.startsWith("0x") ? resultHex.substring(2) : resultHex;
    if (h.length() < 40) return null;
    String addr = "0x" + h.substring(h.length() - 40);
    if (addr.equals("0x0000000000000000000000000000000000000000")) return null;
    return addr;
  }

  public static String decodeBytesHex(String resultHex) {
    if (resultHex == null || resultHex.equals("0x") || resultHex.length() < 2 + 64 + 64) return null;
    String h = resultHex.startsWith("0x") ? resultHex.substring(2) : resultHex;
    int length;
    try {
      length = Integer.parseInt(h.substring(64, 128), 16);
    } catch (NumberFormatException e) {
      return null;
    }
    if (length <= 0) return "0x";
    int dataStart = 128;
    int hexLen = length * 2;
    if (h.length() < dataStart + hexLen) return null;
    return "0x" + h.substring(dataStart, dataStart + hexLen);
  }

  public static String decodeString(String resultHex) {
    String bytesHex = decodeBytesHex(resultHex);
    if (bytesHex == null || bytesHex.equals("0x")) return null;
    String h = bytesHex.substring(2);
    byte[] data = new byte[h.length() / 2];
    for (int i = 0; i < data.length; i++) {
      data[i] = (byte) Integer.parseInt(h.substring(i * 2, i * 2 + 2), 16);
    }
    return new String(data, java.nio.charset.StandardCharsets.UTF_8);
  }
}
