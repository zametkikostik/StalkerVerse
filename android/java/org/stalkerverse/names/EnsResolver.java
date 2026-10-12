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
 * ENS resolution control-flow over public Ethereum JSON-RPC.
 * Does not use public DNS for the .eth name.
 * Full namehash/contenthash eth_call still needs a maintained ENS client before release.
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

    Exception last = null;
    for (String rpc : ETH_RPCS) {
      try {
        trace.add("rpc " + rpc);
        JSONObject chain = rpcCall(rpc, "eth_chainId", new JSONArray());
        if (chain == null || !chain.has("result")) {
          trace.add("rpc skip (no chainId)");
          continue;
        }
        trace.add("rpc live chainId=" + chain.optString("result"));
        return new ResolveResult(
            hostname,
            "ens",
            false,
            null,
            null,
            Arrays.asList("rpc_ok:" + rpc),
            trace,
            "ENS eth_call namehash/contenthash not yet wired in this stub — "
                + "link ens-android library or port TypeScript engine before release");
      } catch (Exception e) {
        last = e;
        Log.w(TAG, "RPC failed: " + rpc, e);
        trace.add("rpc error " + e.getMessage());
      }
    }
    String err = last != null ? last.getMessage() : "All Ethereum RPCs failed";
    return new ResolveResult(hostname, "ens", false, null, null, List.of(), trace, err);
  }

  private static JSONObject rpcCall(String endpoint, String method, JSONArray params)
      throws Exception {
    JSONObject body = new JSONObject();
    body.put("jsonrpc", "2.0");
    body.put("id", 1);
    body.put("method", method);
    body.put("params", params);

    HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
    conn.setRequestMethod("POST");
    conn.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(8));
    conn.setReadTimeout((int) TimeUnit.SECONDS.toMillis(12));
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
    return new JSONObject(sb.toString());
  }
}
