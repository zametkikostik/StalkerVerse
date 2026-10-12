# StalkerVerse

**Privacy-first Chromium browser with native Web3 naming**

Не форк Brave. Не обёртка над WebView.  
Чистый **Chromium overlay** + свой слой StalkerVerse (MPL-2.0).

```
Chromium (Blink · V8 · Network · Sandbox)
        │
        ▼
StalkerVerse Layer
├── Web3 Domain Engine   (ENS · HNS · UNS · SNS · TON · Tezos · Namecoin · EmerDNS)
├── DNS Privacy Engine   (AdGuard DoH / DoT / DoQ — только Web2)
├── DApp Bridge          (EIP-1193 / EIP-6963) — planned
├── Wallet Vault         (Android Keystore) — planned
├── Privacy Controls     (anti-FP, ECH/QUIC, no telemetry)
├── Battery Optimizer    — planned
└── Multilingual UI      (EN / RU / BG)
```

**Pinned Chromium:** `154.0.8037.126` · **Target:** `chrome_public_apk` (Android arm64)

---

## Собрать браузер

Полная инструкция: **[BUILD.md](BUILD.md)**

Кратко (нужен сервер ~64 GB RAM, ~600 GB диск):

```bash
git clone https://github.com/zametkikostik/StalkerVerse.git
cd StalkerVerse
export PATH="$HOME/depot_tools:$PATH"   # см. BUILD.md

./scripts/check-runner.sh
./scripts/fetch-chromium.sh             # часы
./scripts/apply-overlay.sh
./scripts/build-apk.sh

adb install -r artifacts/stalkerverse-chrome_public_apk.apk
```

На **бесплатном** GitHub-hosted Action полный Chromium **не** собирается.  
Там гоняются только тесты имён.

---

## Проверить Web3-логику без сервера

```bash
git clone https://github.com/zametkikostik/StalkerVerse.git
cd StalkerVerse
npm test
# 15 tests: classifier, ENS (uniswap.eth), HNS DoH
```

```ts
classifyHostname("uniswap.eth")  // web3 / ens — без public DNS
classifyHostname("nathan.woodburn") // web3 / hns
classifyHostname("example.com")   // web2 — только потом DoH
```

---

## Структура репозитория

```
StalkerVerse/
├── BUILD.md                 ← как собрать APK
├── README.md
├── LICENSE                  ← MPL-2.0
├── package.json
├── scripts/                 ← fetch / overlay / build / preflight
├── src/lib/names/           ← ENS, HNS, engine, tests
├── src/lib/dns/
├── android/java/org/stalkerverse/
├── chromium/patches/        ← recipes 0001–0003
├── docs/
└── .github/workflows/
```

Полный список: [docs/REPO-CONTENTS.md](docs/REPO-CONTENTS.md)

---

## Статус

| Фаза | Что | Состояние |
|------|-----|-----------|
| 0 | Скрипты + pin Chromium | **готово** — ждёт машину |
| 0 | Первый green APK | **не собран** (нет runner) |
| 1 | Browser shell native | после APK |
| 2 | ENS + HNS + classifier | **готово + тесты** |
| 2 | Omnibox hook в Chromium | после APK |
| 3 | AdGuard DoH в Chromium | recipe есть |
| 4–8 | DApp, wallet, ads, battery, release | planned |

---

## Жёсткие правила

1. **Classify hostname до любого public DNS.**
2. **Нет IP-rewrite** без verified contenthash / A-записи.
3. Contenthash → gateway с явным trust.
4. **Нет telemetry.**
5. V1 без DPI-обхода.

---

## Документация

- [BUILD.md](BUILD.md) — **сборка APK**
- [docs/SELF-HOSTED-APK.md](docs/SELF-HOSTED-APK.md)
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)
- [docs/DOMAIN-RESOLUTION.md](docs/DOMAIN-RESOLUTION.md)
- [docs/NAVIGATION-INTERCEPTOR.md](docs/NAVIGATION-INTERCEPTOR.md)
- [docs/PRIVACY-MODEL.md](docs/PRIVACY-MODEL.md)
- [docs/ROADMAP.md](docs/ROADMAP.md)
- [docs/REPO-CONTENTS.md](docs/REPO-CONTENTS.md)
- [android/BUILD.overlay.md](android/BUILD.overlay.md)

## Лицензии

- StalkerVerse: **MPL-2.0** · Chromium: **BSD-style** (upstream)

## Contributing

Порядок: **сначала green APK, потом фичи.**
