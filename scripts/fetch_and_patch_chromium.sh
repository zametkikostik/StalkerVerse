#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
CHROMIUM_PARENT="${1:-$HOME/chromium}"
SRC_DIR="$CHROMIUM_PARENT/src"
echo "Web3 Chromium fetch & patch -> $CHROMIUM_PARENT"
if ! command -v fetch >/dev/null 2>&1; then
  git clone https://chromium.googlesource.com/chromium/tools/depot_tools.git "$HOME/depot_tools"
  export PATH="$HOME/depot_tools:$PATH"
fi
mkdir -p "$CHROMIUM_PARENT" && cd "$CHROMIUM_PARENT"
if [[ ! -d "$SRC_DIR" ]]; then
  fetch --nohooks android
  cd src && gclient sync --with_branch_heads --with_tags -j"$(nproc)"
else
  cd "$SRC_DIR"
fi
bash "$ROOT/scripts/prepare_web3_injection.sh" || true
bash "$ROOT/scripts/apply_patches.sh"
echo "Next: gn args out/Web3Default && autoninja -C out/Web3Default chrome_public_apk"
