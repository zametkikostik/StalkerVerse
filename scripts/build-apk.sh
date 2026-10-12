#!/usr/bin/env bash
set -euo pipefail
SRC_DIR="${CHROMIUM_SRC:-$HOME/chromium/src}"
OUT_DIR="${CHROMIUM_OUT:-out/StalkerVerse}"
TARGET="${GN_TARGET:-chrome_public_apk}"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$SRC_DIR"
if [[ ! -f BUILD.gn ]]; then echo "incomplete tree"; exit 1; fi
mkdir -p "$OUT_DIR"
if [[ ! -f "$OUT_DIR/args.gn" ]] || [[ "${FORCE_GN_GEN:-0}" == "1" ]]; then
  cat >"$OUT_DIR/args.gn" <<'EOF'
target_os = "android"
target_cpu = "arm64"
is_debug = false
is_official_build = true
is_component_build = false
symbol_level = 0
blink_symbol_level = 0
proprietary_codecs = false
ffmpeg_branding = "Chromium"
EOF
fi
gn gen "$OUT_DIR"
NINJA_BIN="autoninja"; command -v autoninja >/dev/null 2>&1 || NINJA_BIN="ninja"
"$NINJA_BIN" -C "$OUT_DIR" "$TARGET"
mapfile -t APKS < <(find "$OUT_DIR" -type f \( -name 'ChromePublic.apk' -o -name '*chrome*public*.apk' -o -name '*.apk' \) 2>/dev/null | head -20)
if [[ ${#APKS[@]} -eq 0 ]]; then echo "no APK"; exit 1; fi
ARTIFACT_DIR="${STALKERVERSE_ARTIFACTS:-$ROOT/artifacts}"
mkdir -p "$ARTIFACT_DIR"
cp -f "${APKS[0]}" "$ARTIFACT_DIR/stalkerverse-chrome_public_apk.apk"
echo "[stalkerverse] BUILD OK → $ARTIFACT_DIR/stalkerverse-chrome_public_apk.apk"
