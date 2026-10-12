# Android GN overlay notes

These Java sources under `android/java/org/stalkerverse/` are **not** compiled until copied or referenced from the Chromium tree’s `chrome/android` GN targets.

## Integration steps (on build machine)

1. Copy or symlink into Chromium, e.g.:
   ```text
   $CHROMIUM_SRC/chrome/android/java/src/org/stalkerverse/
   ```
2. Add to the appropriate `android_library` / `chrome_java` target in `chrome/android/BUILD.gn` (exact target name depends on pin 154).
3. Depend on AndroidX annotations if not already on classpath (`androidx.annotation.Nullable`).
4. For `EnsResolver`, ensure `org.json` is available (Android SDK provides it).
5. Call `NavigationGate.decide(text)` from omnibox commit path (see `docs/NAVIGATION-INTERCEPTOR.md`).

## Keep in sync

| TypeScript | Java |
|------------|------|
| `src/lib/names/namespaces.ts` | `TldTables.java` |
| `src/lib/names/match.ts` | `NamespaceClassifier.matchProviderId` |
| `src/lib/names/detector.ts` | `NamespaceClassifier.classify*` |
| `src/lib/names/contenthash.ts` | (next) `ContenthashDecoder.java` |
