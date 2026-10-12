package org.stalkerverse.names;

import android.util.Log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Handshake resolver contract — HNS DoH only, never ICANN public DNS. */
public final class HnsResolver {
  private static final String TAG = "StalkerVerseHNS";

  public static final List<String> HNS_DOH =
      Arrays.asList(
          "https://dns.easyhns.com/dns-query", "https://hnsdoh.com/dns-query");

  public static final String HNS_GATEWAY = "https://hns.to";

  private HnsResolver() {}

  public static ResolveResult resolve(String hostname) {
    List<String> trace = new ArrayList<>();
    String name = hostname.toLowerCase(java.util.Locale.ROOT).trim();
    if (name.endsWith(".")) name = name.substring(0, name.length() - 1);
    trace.add("canonicalize → " + name);
    trace.add("namespace HNS");
    trace.add("provider hns — HNS DoH only, not ICANN public DNS");
    for (String ep : HNS_DOH) trace.add("doh endpoint " + ep);
    Log.i(TAG, "HNS resolve requested for " + name + " (native DoH wire pending)");
    return new ResolveResult(
        name,
        "hns",
        false,
        null,
        null,
        List.of(),
        trace,
        "HNS DoH wire not yet in APK — TS engine verified; port dns-wire after first APK");
  }

  public static String gatewayUrl(String hostname) {
    String name = hostname.toLowerCase(java.util.Locale.ROOT).trim().replaceAll("\\.$", "");
    return HNS_GATEWAY + "/" + name;
  }
}
