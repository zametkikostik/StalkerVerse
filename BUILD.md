# Как собрать StalkerVerse (браузер)

Этот репозиторий — **overlay** (слой поверх Chromium), а не полный исходник Chromium.  
Chromium скачивается отдельно (~600 GB). APK собирается на мощной Linux-машине.

**Бесплатный GitHub-hosted runner полный Chromium не потянет** (мало RAM/диска).

---

## 1. Что уже в репозитории

| Путь | Назначение |
|------|------------|
| `src/lib/names/` | ENS + HNS + classifier (TypeScript, с тестами) |
| `src/lib/dns/` | Профили AdGuard DoH/DoT/DoQ |
| `android/java/org/stalkerverse/` | Тот же слой на Java для APK |
| `chromium/patches/` | Рецепты патчей (branding, DNS, omnibox) |
| `scripts/` | fetch / overlay / build / preflight |
| `docs/` | Архитектура, privacy, roadmap |
| `.github/workflows/` | CI: тесты names + self-hosted APK |

---

## 2. Проверка логики имён (любой ПК, бесплатно)

Нужен только **Node.js 20+**.

```bash
git clone https://github.com/zametkikostik/StalkerVerse.git
cd StalkerVerse
node --experimental-strip-types --test src/lib/names/*.test.ts
# или: npm test
```

Ожидается **15 passed**: classifier, ENS namehash, live `uniswap.eth`, HNS DoH.

Это **не** APK, но подтверждает, что Web3-resolve работает.

---

## 3. Сборка Android APK (нужен сервер / мощный ПК)

### Требования к машине

| Ресурс | Минимум | Рекомендуется |
|--------|---------|---------------|
| ОС | Linux x86_64 | Ubuntu 22.04+ |
| Диск | 500 GB свободно | 800 GB+ SSD |
| RAM | 32 GB | 64 GB |
| CPU | 8 ядер | 16+ |
| Сеть | доступ к googlesource | стабильный канал |

### Шаг A — depot_tools

```bash
git clone https://chromium.googlesource.com/chromium/tools/depot_tools.git ~/depot_tools
export PATH="$HOME/depot_tools:$PATH"
```

### Шаг B — этот репозиторий

```bash
git clone https://github.com/zametkikostik/StalkerVerse.git
cd StalkerVerse
chmod +x scripts/*.sh
./scripts/check-runner.sh
```

Если `FAIL` по диску/RAM — машину нужно усилить.

### Шаг C — скачать Chromium (долго, часы)

```bash
export CHROMIUM_VERSION=154.0.8037.126
./scripts/fetch-chromium.sh
```

Если тег не находится:

```bash
export CHROMIUM_REVISION=<commit_sha>
./scripts/fetch-chromium.sh
```

### Шаг D — наложить StalkerVerse

```bash
./scripts/apply-overlay.sh
```

**Первый green APK** может быть stock `chrome_public_apk` без своих `.patch`.

### Шаг E — собрать APK

```bash
./scripts/build-apk.sh
```

Результат: `artifacts/stalkerverse-chrome_public_apk.apk`

### Шаг F — установить

```bash
adb install -r artifacts/stalkerverse-chrome_public_apk.apk
```

---

## 4. После первого green APK

1. Сгенерировать `.patch` по recipes `0001`–`0003`
2. Branding → StalkerVerse
3. Omnibox → `NavigationGate.decide`
4. Java в `BUILD.gn` (см. `android/BUILD.overlay.md`)
5. ENS/HNS из omnibox
6. AdGuard DoH только для Web2

Правило: **classify до public DNS**.

---

## 5. CI

| Job | Runner | Что |
|-----|--------|-----|
| `names-ci` | ubuntu-latest (бесплатно) | тесты names |
| `android-apk` | self-hosted | полный build |

Labels self-hosted: `self-hosted, linux, x64, stalkerverse-chromium`

---

## 6. Частые ошибки

| Проблема | Решение |
|----------|---------|
| Disk full | ≥500 GB; `gclient sync` |
| OOM ninja | `ninja -j4` |
| Tag 154 not found | `CHROMIUM_REVISION=...` |
| gn not found | `gclient runhooks` |
| Java не в APK | ожидаемо для first green |

## 7. Чего нет в репо (намеренно)

- Полного дерева Chromium
- Готовых binary `.patch` (генерируются на pin-дереве)
- Подписанного release APK

Репозиторий = полный исходный слой + инструкция для сборки на сервере.
