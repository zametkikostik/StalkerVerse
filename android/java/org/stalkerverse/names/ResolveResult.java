package org.stalkerverse.names;

import androidx.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/** Outcome of a Web3 resolve attempt. */
public final class ResolveResult {
  public enum Trust {
    DIRECT,
    GATEWAY,
    DNS
  }

  public final String hostname;
  public final String providerId;
  public final boolean verified;
  @Nullable public final String targetUrl;
  @Nullable public final Trust trust;
  public final List<String> records;
  public final List<String> trace;
  @Nullable public final String error;

  public ResolveResult(
      String hostname,
      String providerId,
      boolean verified,
      @Nullable String targetUrl,
      @Nullable Trust trust,
      List<String> records,
      List<String> trace,
      @Nullable String error) {
    this.hostname = hostname;
    this.providerId = providerId;
    this.verified = verified;
    this.targetUrl = targetUrl;
    this.trust = trust;
    this.records = records == null ? Collections.emptyList() : records;
    this.trace = trace == null ? Collections.emptyList() : trace;
    this.error = error;
  }

  public boolean hasNavigableTarget() {
    return targetUrl != null && !targetUrl.isEmpty();
  }
}
