#!/usr/bin/env bash
# Phase 0: fetch pinned Chromium for Android APK builds.
# Requires: depot_tools, ~600 GB free disk, high-memory machine.
set -euo pipefail

PINNED_VERSION="${CHROMIUM_VERSION:-154.0.8037.126}"
SRC_DIR="${CHROMIUM_SRC:-$HOME/chromium/src}"

echo "[stalkerverse] Pinned Chromium: $PINNED_VERSION"
echo "[stalkerverse] Target src: $SRC_DIR"

if ! command -v fetch >/dev/null 2>&1; then
  echo "depot_tools not on PATH. Install: https://commondatastorage.googleapis.com/chrome-infra-docs/flat/depot_tools/docs/html/depot_tools_tutorial.html"
  exit 1
fi

mkdir -p "$(dirname "$SRC_DIR")"
cd "$(dirname "$SRC_DIR")"

if [[ ! -d "$SRC_DIR" ]]; then
  echo "[stalkerverse] fetch --nohooks android..."
  fetch --nohooks android
fi

cd "$SRC_DIR"
echo "[stalkerverse] sync to $PINNED_VERSION"
git fetch --tags origin
# Prefer tag if present; else use version string documented in release notes
if git rev-parse "$PINNED_VERSION" >/dev/null 2>&1; then
  git checkout "$PINNED_VERSION"
else
  echo "[stalkerverse] WARNING: tag $PINNED_VERSION not found locally — set CHROMIUM_REVISION explicitly"
fi

gclient sync --with_branch_heads --with_tags -D

echo "[stalkerverse] fetch complete. Next: ./scripts/apply-overlay.sh"
