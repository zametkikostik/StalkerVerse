#!/usr/bin/env bash
# Apply StalkerVerse patches onto Chromium src tree (out of tree overlay).
set -euo pipefail

SRC_DIR="${CHROMIUM_SRC:-$HOME/chromium/src}"
OVERLAY_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PATCH_DIR="$OVERLAY_ROOT/chromium/patches"

if [[ ! -d "$SRC_DIR" ]]; then
  echo "Chromium src not found at $SRC_DIR — run fetch-chromium.sh first"
  exit 1
fi

echo "[stalkerverse] Applying patches from $PATCH_DIR"
shopt -s nullglob
for p in "$PATCH_DIR"/*.patch; do
  echo "  → $(basename "$p")"
  git -C "$SRC_DIR" apply --verbose --reject --whitespace=fix "$p" || {
    echo "Patch failed: $p"
    exit 1
  }
done

if [[ -d "$OVERLAY_ROOT/android" ]]; then
  echo "[stalkerverse] Overlay android resources staged (manual merge into chrome/android as needed)"
fi

echo "[stalkerverse] Overlay applied. Next: ./scripts/build-apk.sh"
