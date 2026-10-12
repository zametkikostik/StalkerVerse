#!/usr/bin/env bash
set -euo pipefail
PINNED_VERSION="${CHROMIUM_VERSION:-154.0.8037.126}"
PINNED_REVISION="${CHROMIUM_REVISION:-}"
SRC_DIR="${CHROMIUM_SRC:-$HOME/chromium/src}"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
echo "[stalkerverse] Pinned Chromium: $PINNED_VERSION"
if [[ "${SKIP_PREFLIGHT:-0}" != "1" ]] && [[ -x "$ROOT/scripts/check-runner.sh" ]]; then
  "$ROOT/scripts/check-runner.sh" || { echo "Preflight failed"; exit 1; }
fi
if ! command -v fetch >/dev/null 2>&1; then
  echo "depot_tools not on PATH"; exit 1
fi
mkdir -p "$(dirname "$SRC_DIR")"
PARENT="$(dirname "$SRC_DIR")"
cd "$PARENT"
if [[ ! -d "$SRC_DIR" ]] && [[ ! -f .gclient ]]; then
  echo "[stalkerverse] fetch --nohooks android..."
  fetch --nohooks android
fi
if [[ ! -d "$SRC_DIR" ]] && [[ -d "$PARENT/src" ]]; then SRC_DIR="$PARENT/src"; fi
if [[ ! -d "$SRC_DIR" ]]; then echo "src not found"; exit 1; fi
cd "$SRC_DIR"
git fetch --tags origin 2>/dev/null || true
if [[ -n "$PINNED_REVISION" ]]; then git checkout "$PINNED_REVISION"
elif git rev-parse "$PINNED_VERSION" >/dev/null 2>&1; then git checkout "$PINNED_VERSION"
else echo "WARNING: ref $PINNED_VERSION not found — set CHROMIUM_REVISION"; fi
gclient sync --with_branch_heads --with_tags -D
echo "[stalkerverse] fetch complete → apply-overlay.sh && build-apk.sh"
