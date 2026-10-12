export type TrustLabel = "direct" | "gateway" | "dns";

export type ProviderId =
  | "ens"
  | "hns"
  | "uns"
  | "sns"
  | "ton"
  | "tezos"
  | "namecoin"
  | "emerdns";

export type ResolvePath = "web3" | "web2" | "search" | "invalid";

export interface Classification {
  path: ResolvePath;
  hostname: string | null;
  providerId: ProviderId | null;
  searchQuery?: string;
  reason: string;
}

export interface ResolveRecord {
  type: string;
  value: string;
}

export interface NavigationTarget {
  kind: "https" | "ipfs" | "ipns" | "arweave" | "swarm";
  url: string;
  hostname: string;
  trust: TrustLabel;
  cid?: string;
  gateway?: string;
}

export interface VerificationResult {
  ok: boolean;
  reason: string;
}

export interface ResolveResult {
  hostname: string;
  namespace: "web3" | "web2";
  providerId: string;
  providerName: string;
  verified: boolean;
  records: ResolveRecord[];
  target: NavigationTarget | null;
  verification: VerificationResult;
  error: string | null;
  trace: string[];
  cached: boolean;
  resolvedAt: number;
  ttlMs: number;
}

export interface Web3NameProvider {
  id: ProviderId;
  displayName: string;
  priority: number;
  matches: (hostname: string) => boolean;
  resolve: (request: { hostname: string; originalInput: string }) => Promise<ResolveResult>;
  verify: (result: ResolveResult) => Promise<VerificationResult>;
  buildNavigationTarget: (result: ResolveResult) => NavigationTarget | null;
}
