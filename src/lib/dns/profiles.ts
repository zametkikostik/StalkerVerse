export type DnsTransport = "doh" | "dot" | "doq";

export interface DnsProfile {
  id: string;
  label: string;
  transport: DnsTransport;
  /** Used only on Web2 path after Web3 classifier miss */
  endpoint: string;
  provider: "adguard" | "system" | "custom";
}

export const DNS_PROFILES: DnsProfile[] = [
  {
    id: "adguard-doh",
    label: "AdGuard DNS-over-HTTPS",
    transport: "doh",
    endpoint: "https://dns.adguard-dns.com/dns-query",
    provider: "adguard",
  },
  {
    id: "adguard-dot",
    label: "AdGuard DNS-over-TLS",
    transport: "dot",
    endpoint: "tls://dns.adguard-dns.com",
    provider: "adguard",
  },
  {
    id: "adguard-doq",
    label: "AdGuard DNS-over-QUIC",
    transport: "doq",
    endpoint: "quic://dns.adguard-dns.com",
    provider: "adguard",
  },
  {
    id: "system",
    label: "System DNS",
    transport: "doh",
    endpoint: "system",
    provider: "system",
  },
];

export const DEFAULT_DNS_PROFILE_ID = "adguard-doh";

export function dnsEndpoint(profileId: string): string {
  return DNS_PROFILES.find((p) => p.id === profileId)?.endpoint ?? DNS_PROFILES[0]!.endpoint;
}
