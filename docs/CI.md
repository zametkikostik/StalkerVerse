# CI — GitHub Actions

## Workflow: `Android Debug APK`

File: [`.github/workflows/android-debug.yml`](../.github/workflows/android-debug.yml)

**Triggers**
- push / PR to `main` (changes under `android/`)
- manual: **Actions → Android Debug APK → Run workflow**

**What it does**
1. JDK 17 + Android SDK
2. Writes `android/local.properties` (`sdk.dir` + optional Reown ID)
3. `./gradlew :app:assembleDebug`
4. Uploads APK as artifact **web3-browser-debug** (14 days)

## Download APK

1. Open repo → **Actions**
2. Select latest green run of **Android Debug APK**
3. **Artifacts** → `web3-browser-debug` → download zip

## Optional secret

| Secret | Purpose |
|--------|--------|
| `REOWN_PROJECT_ID` | Real WalletConnect Project ID from https://cloud.reown.com |

Settings → Secrets and variables → Actions → New repository secret.

Without it, CI uses placeholder `github-actions-placeholder` (WC connect will not work until set).

## Local parity

```bash
cd android
cp local.properties.example local.properties
# sdk.dir=...
# reown.project.id=...
./gradlew :app:assembleDebug
```
