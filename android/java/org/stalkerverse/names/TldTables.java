package org.stalkerverse.names;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** TLD tables for pure local classification. Keep in sync with src/lib/names/namespaces.ts */
public final class TldTables {
  private TldTables() {}

  public static final Set<String> ENS = set("eth");

  public static final Set<String> UNS =
      set("crypto", "nft", "x", "wallet", "bitcoin", "dao", "888", "blockchain", "hi", "go", "zil", "coin");

  public static final Set<String> SNS = set("sol");
  public static final Set<String> TON = set("ton");
  public static final Set<String> TEZOS = set("tez");
  public static final Set<String> NAMECOIN = set("bit");
  public static final Set<String> EMERDNS = set("emc", "lib", "bazar");

  public static final Set<String> ICANN_LIKE =
      set(
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
          "asia", "cat", "jobs", "mobi", "tel", "travel", "xxx", "post", "geo");

  private static Set<String> set(String... values) {
    HashSet<String> s = new HashSet<>();
    Collections.addAll(s, values);
    return Collections.unmodifiableSet(s);
  }
}
