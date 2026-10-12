package org.stalkerverse.names;

import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * ENS resolution over public Ethereum JSON-RPC.
 * Does not use public DNS for the .eth name.
 * Flow: namehash → registry.resolver → contenthash/addr/text(url) → EnsContenthash.
 */
public final class EnsResolver {
  private static final String TAG = "StalkerVerseENS";

  public static final List<String> ETH_RPCS =
      Arrays.asList(
          "https://ethereum.publicnode.com",
          "https://eth.llamarpc.com",
          "https://cloudflare-eth.com");

  private EnsResolver() {}

  public static ResolveResult resolve(String hostname) {
    List<String> trace = new ArrayList<>();
    trace.add("canonicalize → " + hostname);
    trace.add("namespace ENS (.eth)");
    trace.add("provider ens — Ethereum RPC, not public DNS");

    String name = hostname.toLowerCase(java.util.Locale.ROOT).trim();
    if (name.endsWith(".")) name = name.substring(0, name.length() - 1);

    byte[] node;
    try {
      node = EnsNamehash.namehash(name);
      trace.add("namehash " + EnsNamehash.namehashHex(name));
    } catch (Exception e) {
      trace.add("namehash error " + e.getMessage());
      return fail(hostname, trace, "namehash failed: " + e.getMessage());
    }

    Exception last = null;
    for (String rpc : ETH_RPCS) {
      try {
        trace.add("rpc " + rpc);
        return resolveOnRpc(hostname, node, rpc, trace);
      } catch (Exception e) {
        last = e;
        Log.w(TAG, "RPC failed: " + rpc, e);
        trace.add("rpc error " + e.getMessage());
      }
    }
    String err = last != null ? last.getMessage() : "All Ethereum RPCs failed";
    return fail(hostname, trace, err);
  }

  private static ResolveResult resolveOnRpc(
      String hostname, byte[] node, String rpc, List<String> trace) throws Exception {
    String resolverRaw =
        ethCall(rpc, EnsAbi.REGISTRY, EnsAbi.callDataNode(EnsAbi.SEL_RESOLVER, node));
    String resolver = EnsAbi.decodeAddress(resolverRaw);
    if (resolver == null) {
      trace.add("resolver none");
      return EnsContenthash.fromRecords(hostname, null, null, null, trace);
    }
    trace.add("resolver " + resolver);

    String contenthashHex = null;
    String addr = null;
    String urlText = null;

    try {
      String chRaw =
          ethCall(rpc, resolver, EnsAbi.callDataNode(EnsAbi.SEL_CONTENTHASH, node));
      contenthashHex = EnsAbi.decodeBytesHex(chRaw);
      if (contenthashHex != null && !contenthashHex.equals("0x")) {
        String shown =
            contenthashHex.length() > 18
                ? contenthashHex.substring(0, 18) + "…"
                : contenthashHex;
        trace.add("contenthash " + shown);
      }
    } catch (Exception e) {
      trace.add("contenthash skip " + e.getMessage());
    }

    try {
      String addrRaw = ethCall(rpc, resolver, EnsAbi.callDataNode(EnsAbi.SEL_ADDR, node));
      addr = EnsAbi.decodeAddress(addrRaw);
      if (addr != null) trace.add("addr " + addr);
    } catch (Exception e) {
      trace.add("addr skip " + e.getMessage());
    }

    try {
      String textRaw = ethCall(rpc, resolver, EnsAbi.callDataText(node, "url"));
      urlText = EnsAbi.decodeString(textRaw);
      if (urlText != null && !urlText.isEmpty()) trace.add("url " + urlText);
    } catch (Exception e) {
      trace.add("url text skip " + e.getMessage());
    }

    return EnsContenthash.fromRecords(hostname, contenthashHex, urlText, addr, trace);
  }

  private static ResolveResult fail(String hostname, List<String> trace, String err) {
    return new ResolveResult(hostname, "ens", false, null, null, List.of(), trace, err);
  }

  private static String ethCall(String endpoint, String to, String data) throws Exception {
    JSONArray params = new JSONArray();
    JSONObject call = new JSONObject();
    call.put("to", to);
    call.put("data", data);
    params.put(call);
    params.put("latest");

    JSONObject body = new JSONObject();
    body.put("jsonrpc", "2.0");
    body.put("id", 1);
    body.put("method", "eth_call");
    body.put("params", params);

    HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
    conn.setRequestMethod("POST");
    conn.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(10));
    conn.setReadTimeout((int) TimeUnit.SECONDS.toMillis(15));
    conn.setDoOutput(true);
    conn.setRequestProperty("Content-Type", "application/json");
    byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
    try (OutputStream os = conn.getOutputStream()) {
      os.write(bytes);
    }
    int code = conn.getResponseCode();
    BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(
                code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream(),
                StandardCharsets.UTF_8));
    StringBuilder sb = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) sb.append(line);
    reader.close();
    if (code < 200 || code >= 300) {
      throw new IllegalStateException("HTTP " + code + " " + sb);
    }
    JSONObject resp = new JSONObject(sb.toString());
    if (resp.has("error")) {
      throw new IllegalStateException(resp.getJSONObject("error").toString());
    }
    return resp.optString("result", "0x");
  }
}
