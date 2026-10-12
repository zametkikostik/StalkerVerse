#!/usr/bin/env bash
set -euo pipefail
SRC_DIR="${CHROMIUM_SRC:-$HOME/chromium/src}"
OVERLAY_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PATCH_DIR="$OVERLAY_ROOT/chromium/patches"
JAVA_SRC="$OVERLAY_ROOT/android/java/org/stalkerverse"
JAVA_DST="$SRC_DIR/chrome/android/java/src/org/stalkerverse"
if [[ ! -d "$SRC_DIR" ]]; then echo "no Chromium at $SRC_DIR"; exit 1; fi
if [[ -d "$JAVA_SRC" ]]; then
  mkdir -p "$JAVA_DST"
  if command -v rsync >/dev/null 2>&1; then rsync -a --delete "$JAVA_SRC/" "$JAVA_DST/"
  else cp -a "$JAVA_SRC/." "$JAVA_DST/"; fi
fi
shopt -s nullglob
PATCHES=("$PATCH_DIR"/*.patch)
if [[ ${#PATCHES[@]} -eq 0 ]]; then
  echo "[stalkerverse] No *.patch — stock chrome_public_apk OK for first green"
else
  cd "$SRC_DIR"
  for p in $(printf '%s\n' "${PATCHES[@]}" | sort); do
    git apply --check "$p" && git apply "$p"
  done
fi
shopt -u nullglob
mkdir -p "$SRC_DIR/stalkerverse"
echo "overlay_applied=$(date -u +%Y-%m-%dT%H:%M:%SZ)" >"$SRC_DIR/stalkerverse/OVERLAY_STAMP"
echo "[stalkerverse] overlay applied"
