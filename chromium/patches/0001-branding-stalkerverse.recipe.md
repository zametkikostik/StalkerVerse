# Recipe: 0001-branding-stalkerverse

**Pin:** Chromium `154.0.8037.126`  
**Apply after:** clean tree at pin  
**Produce:** `git -C $CHROMIUM_SRC diff > chromium/patches/0001-branding-stalkerverse.patch`

## Intent

Rename user-visible “Chrome” / “Chromium” product strings for the Android public APK shell to **StalkerVerse**, without claiming Google trademarks in store listings.

## Touch points (typical paths; verify on pin)

1. `chrome/app/theme/chromium/BRANDING` or Android string resources  
2. `chrome/android/java/res/values/channel_constants.xml` / `chrome_app_strings.grd`  
3. `chrome/app/chromium_strings.grd` — `IDS_PRODUCT_NAME`, short name  
4. Android launcher label in `chrome/android/java/res/values/strings.xml` equivalents  

## Required string map

| Key concept | Value |
|-------------|--------|
| Product name | StalkerVerse |
| Short name | StalkerVerse |
| User-agent product token | StalkerVerse |
| About dialog title | StalkerVerse |

## Do not change

- Google API keys / OAuth client IDs (leave empty or project-owned)
- Crash server endpoints (disable telemetry per PRIVACY-MODEL)
- Internal class names that break component build

## Verification

```bash
aapt dump badging out/StalkerVerse/apks/ChromePublic.apk | grep -i application-label
# expect StalkerVerse
```
