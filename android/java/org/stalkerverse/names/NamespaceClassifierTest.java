package org.stalkerverse.names;

/**
 * JVM-side expectations mirroring src/lib/names/detector.test.ts.
 * Documents required invariants for the Android port.
 */
public final class NamespaceClassifierTest {
  private NamespaceClassifierTest() {}

  public static void assertInvariants() {
    Classification eth = NamespaceClassifier.classifyHostname("alice.eth");
    require(eth.isWeb3() && "ens".equals(eth.providerId), "alice.eth → ens");

    Classification sub = NamespaceClassifier.classifyHostname("foo.alice.eth");
    require(sub.isWeb3() && "ens".equals(sub.providerId), "sub.eth → ens");

    Classification com = NamespaceClassifier.classifyHostname("example.com");
    require(com.isWeb2() && com.providerId == null, "example.com → web2");

    require("uns".equals(NamespaceClassifier.matchProviderId("name.crypto")), "uns");
    require("sns".equals(NamespaceClassifier.matchProviderId("name.sol")), "sns");
    require("hns".equals(NamespaceClassifier.matchProviderId("example.myhnszone")), "hns");
    require(NamespaceClassifier.matchProviderId("example.com") == null, "com not hns");

    Classification search = NamespaceClassifier.classifyInput("what is ens");
    require(search.path == Classification.Path.SEARCH, "search");
  }

  private static void require(boolean cond, String msg) {
    if (!cond) {
      throw new IllegalStateException("Invariant failed: " + msg);
    }
  }

  public static void main(String[] args) {
    assertInvariants();
    System.out.println("NamespaceClassifier invariants OK");
  }
}
