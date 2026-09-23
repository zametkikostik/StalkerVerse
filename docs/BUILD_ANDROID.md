# Build debug APK

```bash
cd android
cp local.properties.example local.properties
# set sdk.dir and reown.project.id
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Test: Settings DPI, Ads, Wallet, window.ethereum in page console.
Full Chromium: scripts/fetch_and_patch_chromium.sh
