#!/usr/bin/env bash
set -euo pipefail
echo "==> Web3 Chromium Browser - Dev setup"
if command -v python3 >/dev/null; then echo "[OK] Python3 found"; else echo "[!] Python3 not found"; fi
chmod +x "$(dirname "$0")"/*.sh 2>/dev/null || true
echo "Next: docs/BUILD.md | python3 modules/dpi-proxy/dpi_proxy.py"
