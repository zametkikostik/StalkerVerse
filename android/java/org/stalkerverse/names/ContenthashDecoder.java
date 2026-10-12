package org.stalkerverse.names;

import androidx.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * EIP-1577 contenthash decoder — port of src/lib/names/contenthash.ts.
 * Never treats a raw IP as a navigation target.
 */
public final class ContenthashDecoder {
  private static final Pattern DNS_NAME =
      Pattern.compile("[a-z0-9][a-z0-9.-]+\\.[a-z]{2,}", Pattern.CASE_INSENSITIVE);

  private ContenthashDecoder() {}

  public enum Protocol {
    IPFS,
    IPNS,
    SWARM,
    ARWEAVE
  }

  public static final class Decoded {
    public final Protocol protocol;
    public final String value;

    public Decoded(Protocol protocol, String value) {
      this.protocol = protocol;
      this.value = value;
    }
  }

  @Nullable
  public static Decoded decode(@Nullable String hex) {
    if (hex == null || hex.isEmpty() || "0x".equals(hex) || "0x00".equals(hex)) {
      return null;
    }
    byte[] bytes;
    try {
      bytes = hexToBytes(hex);
    } catch (IllegalArgumentException e) {
      return null;
    }
    if (bytes.length < 2) return null;

    if (bytes.length >= 36
        && (bytes[0] & 0xff) == 0x90
        && (bytes[1] & 0xff) == 0xb2
        && (bytes[2] & 0xff) == 0xc6
        && (bytes[3] & 0xff) == 0x1b) {
      return new Decoded(Protocol.ARWEAVE, bytesToHex(bytes, 4, 36));
    }

    int ns = bytes[0] & 0xff;
    byte[] rest = slice(bytes, 1, bytes.length);

    if (ns == 0xe3) return new Decoded(Protocol.IPFS, decodeIpfsLike(rest));
    if (ns == 0xe5) {
      String name = decodeIpnsName(rest);
      return name != null ? new Decoded(Protocol.IPNS, name) : null;
    }
    if (ns == 0xe4) return new Decoded(Protocol.SWARM, bytesToHex(rest, 0, rest.length));
    return null;
  }

  public static String toGatewayUrl(Decoded content, @Nullable String gatewayOrigin) {
    String origin =
        gatewayOrigin == null || gatewayOrigin.isEmpty() ? "https://dweb.link" : gatewayOrigin;
    if (origin.endsWith("/")) origin = origin.substring(0, origin.length() - 1);
    switch (content.protocol) {
      case IPFS:
        return origin + "/ipfs/" + content.value;
      case IPNS:
        return origin + "/ipns/" + content.value;
      case ARWEAVE:
        return "https://arweave.net/" + content.value;
      case SWARM:
        return "bzz://" + content.value;
      default:
        throw new IllegalStateException("unknown protocol");
    }
  }

  public static ResolveResult.Trust trustFor(Decoded content) {
    if (content.protocol == Protocol.SWARM) return ResolveResult.Trust.DIRECT;
    return ResolveResult.Trust.GATEWAY;
  }

  private static String decodeIpfsLike(byte[] rest) {
    String hex = bytesToHex(rest, 0, rest.length);
    if (hex.length() > 58) hex = hex.substring(0, 58);
    return "bafy" + hex;
  }

  @Nullable
  private static String decodeIpnsName(byte[] rest) {
    if (rest.length < 2) return tryIdentityDns(rest);
    int i = 0;
    if ((rest[0] & 0xff) == 0x01 && (rest[1] & 0xff) == 0x01) i = 1;
    if (i >= rest.length || (rest[i] & 0xff) != 0x01) return tryIdentityDns(rest);
    i += 1;
    if (i >= rest.length) return tryIdentityDns(rest);
    i += 1;
    if (i + 1 >= rest.length) return tryIdentityDns(rest);
    if ((rest[i] & 0xff) == 0x00) {
      int len = rest[i + 1] & 0xff;
      if (len > 0 && i + 2 + len <= rest.length) {
        byte[] digest = slice(rest, i + 2, i + 2 + len);
        String text = new String(digest, StandardCharsets.UTF_8);
        if (isDnsLike(text)) return text.toLowerCase(Locale.ROOT);
      }
    }
    return tryIdentityDns(rest);
  }

  @Nullable
  private static String tryIdentityDns(byte[] rest) {
    try {
      String text = new String(rest, StandardCharsets.UTF_8);
      Matcher m = DNS_NAME.matcher(text);
      if (m.find()) return m.group().toLowerCase(Locale.ROOT);
    } catch (Exception ignored) {
    }
    return null;
  }

  private static boolean isDnsLike(String text) {
    if (text == null || text.isEmpty() || !text.contains(".")) return false;
    return text.matches("(?i)[a-z0-9][a-z0-9.-]*");
  }

  static byte[] hexToBytes(String hex) {
    String h = hex.startsWith("0x") || hex.startsWith("0X") ? hex.substring(2) : hex;
    if ((h.length() & 1) != 0) throw new IllegalArgumentException("odd contenthash");
    byte[] out = new byte[h.length() / 2];
    for (int i = 0; i < out.length; i++) {
      out[i] = (byte) Integer.parseInt(h.substring(i * 2, i * 2 + 2), 16);
    }
    return out;
  }

  static String bytesToHex(byte[] bytes, int from, int toExclusive) {
    StringBuilder sb = new StringBuilder((toExclusive - from) * 2);
    for (int i = from; i < toExclusive; i++) {
      sb.append(String.format(Locale.ROOT, "%02x", bytes[i] & 0xff));
    }
    return sb.toString();
  }

  private static byte[] slice(byte[] src, int from, int toExclusive) {
    byte[] out = new byte[toExclusive - from];
    System.arraycopy(src, from, out, 0, out.length);
    return out;
  }
}
