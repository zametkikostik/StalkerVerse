package org.stalkerverse.names;

import androidx.annotation.Nullable;

/**
 * Single entry for omnibox / loadUrl: classify first, then resolve or pass to Web2 DNS.
 * Wire from Chromium Android omnibox commit (docs/NAVIGATION-INTERCEPTOR.md).
 */
public final class NavigationGate {
  private NavigationGate() {}

  public static final class Decision {
    public enum Action {
      LOAD_URL,
      SHOW_WEB3_ERROR,
      FALLTHROUGH_WEB2,
      SEARCH,
      REJECT
    }

    public final Action action;
    @Nullable public final String url;
    @Nullable public final ResolveResult.Trust trust;
    @Nullable public final Classification classification;
    @Nullable public final ResolveResult resolve;
    @Nullable public final String message;

    public Decision(
        Action action,
        @Nullable String url,
        @Nullable ResolveResult.Trust trust,
        @Nullable Classification classification,
        @Nullable ResolveResult resolve,
        @Nullable String message) {
      this.action = action;
      this.url = url;
      this.trust = trust;
      this.classification = classification;
      this.resolve = resolve;
      this.message = message;
    }
  }

  public static Classification peek(String omniboxText) {
    return NamespaceClassifier.classifyInput(omniboxText);
  }

  /** Full gate. Web3 resolve may block — call off UI thread. */
  public static Decision decide(String omniboxText) {
    Classification c = NamespaceClassifier.classifyInput(omniboxText);
    switch (c.path) {
      case INVALID:
        return new Decision(Decision.Action.REJECT, null, null, c, null, c.reason);
      case SEARCH:
        return new Decision(Decision.Action.SEARCH, null, null, c, null, c.searchQuery);
      case WEB2:
        return new Decision(Decision.Action.FALLTHROUGH_WEB2, null, null, c, null, c.reason);
      case WEB3:
        return decideWeb3(c);
      default:
        return new Decision(Decision.Action.REJECT, null, null, c, null, "unknown path");
    }
  }

  private static Decision decideWeb3(Classification c) {
    if (c.hostname == null || c.providerId == null) {
      return new Decision(
          Decision.Action.SHOW_WEB3_ERROR, null, null, c, null, "missing hostname/provider");
    }
    ResolveResult result;
    if ("ens".equals(c.providerId)) {
      result = EnsResolver.resolve(c.hostname);
    } else {
      result =
          new ResolveResult(
              c.hostname,
              c.providerId,
              false,
              null,
              null,
              java.util.List.of(),
              java.util.List.of("provider " + c.providerId + " not wired in native stub"),
              "Provider " + c.providerId + " resolver not implemented in native layer yet");
    }
    if (result.hasNavigableTarget()) {
      return new Decision(
          Decision.Action.LOAD_URL, result.targetUrl, result.trust, c, result, null);
    }
    return new Decision(
        Decision.Action.SHOW_WEB3_ERROR,
        null,
        null,
        c,
        result,
        result.error != null
            ? result.error
            : "Name resolved but no verified content target. Refusing IP rewrite.");
  }
}
