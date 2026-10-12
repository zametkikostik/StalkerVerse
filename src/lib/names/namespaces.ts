/** ENS — never public DNS */
export const ENS_TLDS = new Set(["eth"]);

/** Unstoppable / UNS */
export const UNS_TLDS = new Set([
  "crypto",
  "nft",
  "x",
  "wallet",
  "bitcoin",
  "dao",
  "888",
  "blockchain",
  "hi",
  "go",
  "zil",
  "coin",
]);

/** Solana Name Service */
export const SNS_TLDS = new Set(["sol"]);

/** TON DNS */
export const TON_TLDS = new Set(["ton"]);

/** Tezos Domains */
export const TEZOS_TLDS = new Set(["tez"]);

/** Namecoin */
export const NAMECOIN_TLDS = new Set(["bit"]);

/** EmerDNS */
export const EMERDNS_TLDS = new Set(["emc", "lib", "bazar"]);

/**
 * ICANN-like TLDs that must NOT be claimed by Handshake matcher.
 */
export const ICANN_GTLD_LIST = new Set([
  "com", "org", "net", "edu", "gov", "mil", "int", "info", "biz", "name", "pro",
  "aero", "coop", "museum", "app", "dev", "page", "io", "co", "ai", "xyz",
  "online", "site", "tech", "store", "shop", "blog", "cloud", "me", "tv", "cc",
  "us", "uk", "ru", "bg", "de", "fr", "eu", "cn", "jp", "kr", "in", "br", "au",
  "ca", "mx", "es", "it", "nl", "pl", "tr", "ua", "zip", "mov", "new", "top",
  "icu", "club", "vip", "work", "live", "world", "space", "website", "email",
  "link", "news", "media", "agency", "studio", "design", "art", "game", "games",
  "gg", "to", "fm", "ly", "sh", "so", "id", "ph", "vn", "sg", "hk", "tw", "nz",
  "za", "ar", "cl", "pe", "ch", "at", "be", "se", "no", "dk", "fi", "ie", "pt",
  "gr", "cz", "ro", "hu", "sk", "si", "hr", "rs", "ba", "mk", "al", "lt", "lv",
  "ee", "is", "mt", "cy", "lu", "li", "mc", "ad", "sm", "va", "il", "ae", "sa",
  "qa", "kw", "bh", "om", "eg", "ma", "tn", "dz", "ng", "ke", "gh", "tz", "ug",
  "et", "sn", "ci", "cm", "ao", "mz", "zw", "bw", "na", "mu", "sc", "re", "yt",
  "gf", "gp", "mq", "pf", "nc", "wf", "pm", "tf", "aq", "asia", "cat", "jobs",
  "mobi", "tel", "travel", "xxx", "post", "geo",
]);
