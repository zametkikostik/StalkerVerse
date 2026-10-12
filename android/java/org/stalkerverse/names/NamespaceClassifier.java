package org.stalkerverse.names;

import androidx.annotation.Nullable;
import java.util.Locale;

/** Pure local hostname classification — port of src/lib/names/detector.ts + match.ts. */
public final class NamespaceClassifier {
  private NamespaceClassifier() {}

  public static Classification classifyInput(String raw) {
    if (raw == null) return invalid("empty");
    String value = raw.trim();
    if (value.isEmpty()) return invalid("empty");
    if (value.contains("://") || value.contains(".") || value.endsWith(".eth")) {
      try {
        String host =
            value.contains("://")
                ? new java.net.URI(value).getHost()
                : value.replaceAll("/.*$", "");
        if (host == null) host = value;
        return classifyHostname(host);
      } catch (Exception e) {
        return classifyHostname(value);
      }
    }
    if (value.matches(".*\\s.*") || !value.contains(".")) {
      return new Classification(
          Classification.Path.SEARCH, null, null, value, "search query");
    }
    return classifyHostname(value);
  }

  public static Classification classifyHostname(String input) {
    String host = canonicalize(input);
    if (host == null) return invalid("invalid hostname");
    String provider = matchProviderId(host);
    if (provider != null) {
      return new Classification(
          Classification.Path.WEB3,
          host,
          provider,
          null,
          "web3 provider " + provider + " — no public DNS");
    }
    return new Classification(
        Classification.Path.WEB2, host, null, null, "icann-like / web2 DNS path");
  }

  @Nullable
  public static String matchProviderId(String hostname) {
    String tld = tldOf(hostname);
    if (TldTables.ENS.contains(tld)) return "ens";
    if (TldTables.UNS.contains(tld)) return "uns";
    if (TldTables.SNS.contains(tld)) return "sns";
    if (TldTables.TON.contains(tld)) return "ton";
    if (TldTables.TEZOS.contains(tld)) return "tezos";
    if (TldTables.NAMECOIN.contains(tld)) return "namecoin";
    if (TldTables.EMERDNS.contains(tld)) return "emerdns";
    if (!tld.isEmpty() && !isIcannLike(tld) && !isExplicitWeb3(tld)) return "hns";
    return null;
  }

  public static boolean isIcannLike(String tld) {
    if (tld == null || tld.isEmpty()) return false;
    if (TldTables.ICANN_LIKE.contains(tld)) return true;
    if (tld.startsWith("xn--")) return true;
    return tld.length() == 2 && tld.chars().allMatch(Character::isLetter);
  }

  private static boolean isExplicitWeb3(String tld) {
    return TldTables.ENS.contains(tld)
        || TldTables.UNS.contains(tld)
        || TldTables.SNS.contains(tld)
        || TldTables.TON.contains(tld)
        || TldTables.TEZOS.contains(tld)
        || TldTables.NAMECOIN.contains(tld)
        || TldTables.EMERDNS.contains(tld);
  }

  @Nullable
  static String canonicalize(String input) {
    if (input == null) return null;
    String s = input.trim().toLowerCase(Locale.ROOT);
    if (s.isEmpty()) return null;
    try {
      if (s.contains("://")) {
        String h = new java.net.URI(s).getHost();
        if (h != null) s = h;
      }
    } catch (Exception ignored) {}
    if (s.endsWith(".")) s = s.substring(0, s.length() - 1);
    if (s.startsWith("www.")) s = s.substring(4);
    if (s.length() > 253) return null;
    return s;
  }

  static String tldOf(String hostname) {
    int i = hostname.lastIndexOf('.');
    if (i < 0 || i == hostname.length() - 1) return "";
    return hostname.substring(i + 1);
  }

  private static Classification invalid(String reason) {
    return new Classification(Classification.Path.INVALID, null, null, null, reason);
  }
}
