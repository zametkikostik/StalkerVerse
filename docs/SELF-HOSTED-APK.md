# Self-hosted / сервер: первый APK

См. **[BUILD.md](../BUILD.md)** — главная инструкция.

## Цель

Green `chrome_public_apk` на Linux с большим диском.

## Требования

| Ресурс | Минимум | Рекомендуется |
|--------|---------|---------------|
| Диск | 500 GB free | 800 GB+ SSD |
| RAM | 32 GB | 64 GB |
| CPU | 8 cores | 16+ |
| ОС | Linux x86_64 | Ubuntu 22.04+ |

GitHub free hosted (7 GB RAM) — **не** для Chromium build.

## Установка

```bash
git clone https://chromium.googlesource.com/chromium/tools/depot_tools.git ~/depot_tools
export PATH="$HOME/depot_tools:$PATH"
git clone https://github.com/zametkikostik/StalkerVerse.git
cd StalkerVerse
chmod +x scripts/*.sh
./scripts/check-runner.sh
```

## Сборка

```bash
export CHROMIUM_VERSION=154.0.8037.126
./scripts/fetch-chromium.sh
./scripts/apply-overlay.sh
./scripts/build-apk.sh
adb install -r artifacts/stalkerverse-chrome_public_apk.apk
```

## После green

1. Stock APK OK для первого билда
2. Потом patches 0001–0003 + NavigationGate
3. ENS/HNS уже в TS/Java

Labels CI: `self-hosted, linux, x64, stalkerverse-chromium`
