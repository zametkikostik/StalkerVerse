# Android GN overlay notes

Sources under `android/java/org/stalkerverse/` compile only after copy/symlink into Chromium `chrome/android`.

## Integration steps

1. Copy to `$CHROMIUM_SRC/chrome/android/java/src/org/stalkerverse/`
2. Add to `chrome_java` / `android_library` in `chrome/android/BUILD.gn`
3. AndroidX annotations + `org.json` for EnsResolver
4. Omnibox: `NavigationGate.decide(text)`
5. Web2 DNS: apply `DnsProfiles` selected id to Chromium Secure DNS prefs

## Keep in sync

| TypeScript | Java |
|------------|------|
| `src/lib/names/namespaces.ts` | `TldTables.java` |
| `src/lib/names/match.ts` / `detector.ts` | `NamespaceClassifier.java` |
| `src/lib/names/contenthash.ts` | `ContenthashDecoder.java` |
| `src/lib/dns/profiles.ts` | `dns/DnsProfiles.java` |
