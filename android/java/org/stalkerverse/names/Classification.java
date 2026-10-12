package org.stalkerverse.names;

import androidx.annotation.Nullable;

/** Result of pure local classification. No network was used to produce this. */
public final class Classification {
  public enum Path {
    WEB3,
    WEB2,
    SEARCH,
    INVALID
  }

  public final Path path;
  @Nullable public final String hostname;
  @Nullable public final String providerId;
  @Nullable public final String searchQuery;
  public final String reason;

  public Classification(
      Path path,
      @Nullable String hostname,
      @Nullable String providerId,
      @Nullable String searchQuery,
      String reason) {
    this.path = path;
    this.hostname = hostname;
    this.providerId = providerId;
    this.searchQuery = searchQuery;
    this.reason = reason;
  }

  public boolean isWeb3() {
    return path == Path.WEB3;
  }

  public boolean isWeb2() {
    return path == Path.WEB2;
  }
}
