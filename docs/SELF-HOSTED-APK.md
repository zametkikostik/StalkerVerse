# Self-hosted first APK (Phase 0)

Goal: **green `chrome_public_apk`** on a large runner.

## Requirements

| Resource | Minimum | Recommended |
|----------|---------|-------------|
| Disk | 500 GB free | 800 GB+ SSD |
| RAM | 32 GB | 64 GB |
| CPU | 8 cores | 16+ |
| OS | Linux x86_64 | Ubuntu 22.04+ |

## Setup

```bash
git clone https://chromium.googlesource.com/chromium/tools/depot_tools.git ~/depot_tools
export PATH="$HOME/depot_tools:$PATH"
git clone https://github.com/zametkikostik/StalkerVerse.git
cd StalkerVerse
./scripts/check-runner.sh
```

## Build

```bash
export CHROMIUM_VERSION=154.0.8037.126
./scripts/fetch-chromium.sh    # hours
./scripts/apply-overlay.sh
./scripts/build-apk.sh         # hours first time
```

APK: `artifacts/stalkerverse-chrome_public_apk.apk`

## Stability

1. First APK = stock `chrome_public_apk` (no custom GN required).
2. Java overlay is copied but need not compile for green #1.
3. After green: wire `BUILD.gn`, patches 0001–0003, NavigationGate.
4. ENS/HNS stay tested in TS — only call sites move into Chromium.

## CI

`.github/workflows/android-apk.yml` — self-hosted `linux,x64`.

See also: `android/BUILD.overlay.md`
