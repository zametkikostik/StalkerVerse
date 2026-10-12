#!/usr/bin/env bash
# Build chrome_public_apk for StalkerVerse (Phase 0/1).
set -euo pipefail

SRC_DIR="${CHROMIUM_SRC:-$HOME/chromium/src}"
OUT_DIR="${CHROMIUM_OUT:-out/StalkerVerse}"
TARGET="${GN_TARGET:-chrome_public_apk}"

cd "$SRC_DIR"

if [[ ! -f build/config/gclient_args.gni ]]; then
  echo "Chromium tree incomplete — run fetch-chromium.sh"
  exit 1
fi

echo "[stalkerverse] gn gen $OUT_DIR"
gn gen "$OUT_DIR" --args='
  target_os="android"
  target_cpu="arm64"
  is_debug=false
  is_official_build=true
  is_component_build=false
  symbol_level=0
  blink_symbol_level=0
  proprietary_codecs=false
  ffmpeg_branding="Chromium"
'

echo "[stalkerverse] autoninja -C $OUT_DIR $TARGET"
autoninja -C "$OUT_DIR" "$TARGET"

APK=$(find "$OUT_DIR" -name "*.apk" | head -n 1 || true)
echo "[stalkerverse] Build finished. APK candidate: ${APK:-none found — check $OUT_DIR}"
