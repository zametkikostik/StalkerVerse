package org.stalkerverse.dns;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Encrypted DNS for Web2 only — after NamespaceClassifier returns WEB2. */
public final class DnsProfiles {
  private DnsProfiles() {}

  public enum Transport {
    DOH,
    DOT,
    DOQ,
    SYSTEM
  }

  public static final class Profile {
    public final String id;
    public final String label;
    public final Transport transport;
    public final String endpoint;
    public final String provider;

    public Profile(
        String id, String label, Transport transport, String endpoint, String provider) {
      this.id = id;
      this.label = label;
      this.transport = transport;
      this.endpoint = endpoint;
      this.provider = provider;
    }
  }

  public static final String DEFAULT_ID = "adguard-doh";

  public static final List<Profile> ALL =
      Collections.unmodifiableList(
          Arrays.asList(
              new Profile(
                  "adguard-doh",
                  "AdGuard DNS-over-HTTPS",
                  Transport.DOH,
                  "https://dns.adguard-dns.com/dns-query",
                  "adguard"),
              new Profile(
                  "adguard-dot",
                  "AdGuard DNS-over-TLS",
                  Transport.DOT,
                  "tls://dns.adguard-dns.com",
                  "adguard"),
              new Profile(
                  "adguard-doq",
                  "AdGuard DNS-over-QUIC",
                  Transport.DOQ,
                  "quic://dns.adguard-dns.com",
                  "adguard"),
              new Profile("system", "System DNS", Transport.SYSTEM, "system", "system")));

  public static Profile byId(String id) {
    for (Profile p : ALL) {
      if (p.id.equals(id)) return p;
    }
    return ALL.get(0);
  }

  public static String endpointFor(String profileId) {
    return byId(profileId).endpoint;
  }
}
