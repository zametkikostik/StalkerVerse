#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PATCHES_DIR="${SCRIPT_DIR}/../patches"
if [[ ! -f "BUILD.gn" ]] || [[ ! -d "chrome" ]]; then
  echo "Run from Chromium src/"
  exit 1
fi
echo "==> Applying patches from ${PATCHES_DIR}"
shopt -s nullglob
for patch in "${PATCHES_DIR}"/*.patch; do
  name="$(basename "$patch")"
  echo -n "--> $name ... "
  if [[ "$name" == *placeholder* ]]; then echo "skip"; continue; fi
  if git apply --check "$patch" 2>/dev/null; then git apply "$patch"; echo OK
  elif git apply --reverse --check "$patch" 2>/dev/null; then echo "already applied"
  else echo "CONFLICT - see docs/BUILD.md"; fi
done
