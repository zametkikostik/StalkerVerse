# Web3 Chromium Browser (StalkerVerse)

Полноценный форк Chromium для Android с нативной поддержкой Web3, децентрализованных доменов, защитой от DPI и privacy-friendly таргетингом рекламы.

> **Репозиторий:** https://github.com/zametkikostik/StalkerVerse

> **Статус**: Каркас + рабочие модули. Web3 provider, DPI-proxy, патчи инжекции, Compose UI, HD-wallet, on-device ads.

## Цели проекта

- База: актуальный Chromium (Android)
- Web3: инъекция `window.ethereum` + WalletConnect v2 + поддержка основных сетей
- Домены: `.eth`, Unstoppable Domains, IPFS, Handshake и кастомные TLD
- DPI Protection: локальный proxy с фрагментацией ClientHello/SNI
- Реклама: on-device интересы + явное согласие пользователя
- Privacy-first: минимум телеметрии

## Быстрый старт (Android debug APK)

```bash
cd android
cp local.properties.example local.properties
# укажи sdk.dir и reown.project.id
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Подробнее: [docs/BUILD_ANDROID.md](docs/BUILD_ANDROID.md)

## Сборка Chromium + патчи

```bash
./scripts/fetch_and_patch_chromium.sh ~/chromium
cd ~/chromium/src
gn args out/Web3Default   # см. scripts/args.gn.template
autoninja -C out/Web3Default chrome_public_apk
```

## Структура

```
android/     # Compose UI, WebView shell, DPI Service, Wallet
modules/     # web3 provider, domain-resolver, dpi-proxy, ads
patches/     # Chromium integration guides 0002/0003
scripts/     # fetch, patch, injection prepare
docs/        # BUILD, ARCHITECTURE, WALLET, ADS
```

## Лицензия

Код проекта — Apache 2.0. Chromium — BSD-3-Clause.
