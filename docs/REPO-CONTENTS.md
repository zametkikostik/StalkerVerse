# Содержимое репозитория

Исходники StalkerVerse **без** дерева Chromium.

## Корень
- `README.md`, `BUILD.md`, `LICENSE` (MPL-2.0), `package.json`

## scripts/
- `check-runner.sh`, `fetch-chromium.sh`, `apply-overlay.sh`, `build-apk.sh`

## src/lib/names/
- types, namespaces, match, canonicalize, detector (+test)
- keccak256, ens-namehash, ens-resolve, ens.test, contenthash
- dns-wire, hns-resolve, hns.test, engine, providers-stub

## src/lib/dns/
- `profiles.ts`

## android/java/org/stalkerverse/
- names: Classifier, NavigationGate, ENS stack, ContenthashDecoder, HnsResolver
- dns: DnsProfiles
- `android/BUILD.overlay.md`

## chromium/patches/
- recipes 0001 branding, 0002 DNS, 0003 interceptor

## docs/
- ARCHITECTURE, DOMAIN-RESOLUTION, NAVIGATION-INTERCEPTOR
- PRIVACY-MODEL, ROADMAP, SELF-HOSTED-APK, GROK-MASTER-PROMPT, REPO-CONTENTS

## .github/workflows/
- `names-ci.yml`, `android-apk.yml`
