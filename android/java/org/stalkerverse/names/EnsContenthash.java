package org.stalkerverse.names;

import androidx.annotation.Nullable;

/** Pure helpers: contenthash + optional https text → navigable result. No IP rewrite. */
public final class EnsContenthash {
  private EnsContenthash() {}

  public static ResolveResult fromRecords(
      String hostname,
      @Nullable String contenthashHex,
      @Nullable String urlText,
      @Nullable String addr,
      java.util.List<String> trace) {
    java.util.List<String> records = new java.util.ArrayList<>();
    if (addr != null && !addr.isEmpty()) records.add("addr:" + addr);
    if (contenthashHex != null && !contenthashHex.isEmpty() && !"0x".equals(contenthashHex)) {
      records.add("contenthash:" + contenthashHex);
    }
    if (urlText != null && !urlText.isEmpty()) records.add("url:" + urlText);

    ContenthashDecoder.Decoded decoded = ContenthashDecoder.decode(contenthashHex);
    if (decoded != null) {
      String url = ContenthashDecoder.toGatewayUrl(decoded, "https://dweb.link");
      ResolveResult.Trust trust = ContenthashDecoder.trustFor(decoded);
      java.util.List<String> t = new java.util.ArrayList<>(trace);
      t.add("decoded " + decoded.protocol + " " + decoded.value);
      t.add("target " + url + " trust=" + trust);
      return new ResolveResult(hostname, "ens", true, url, trust, records, t, null);
    }

    if (urlText != null && urlText.startsWith("https://")) {
      java.util.List<String> t = new java.util.ArrayList<>(trace);
      t.add("url text record → https");
      return new ResolveResult(
          hostname, "ens", true, urlText, ResolveResult.Trust.DNS, records, t, null);
    }

    java.util.List<String> t = new java.util.ArrayList<>(trace);
    t.add("no verified contenthash or https url — refusing IP rewrite");
    return new ResolveResult(
        hostname,
        "ens",
        false,
        null,
        null,
        records,
        t,
        "ENS name resolved but has no verified contenthash or https url record. "
            + "Refusing IP rewrite.");
  }
}
