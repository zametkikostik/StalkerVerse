#!/usr/bin/env bash
# Preflight for self-hosted Chromium Android APK builds.
set -euo pipefail

PASS=0; FAIL=0; WARN=0
ok()   { echo "  [OK]   $*"; PASS=$((PASS + 1)); }
bad()  { echo "  [FAIL] $*"; FAIL=$((FAIL + 1)); }
warn() { echo "  [WARN] $*"; WARN=$((WARN + 1)); }

echo "=== StalkerVerse runner preflight ==="
echo "date: $(date -u +%Y-%m-%dT%H:%M:%SZ)"
echo

echo "-- disk --"
if df -BG "${HOME:-/}" >/dev/null 2>&1; then
  avail=$(df -BG "${CHROMIUM_SRC:-$HOME}" 2>/dev/null | awk 'NR==2{gsub(/G/,"",$4); print $4}')
  if [[ -n "${avail:-}" ]] && [[ "$avail" -ge 500 ]]; then
    ok "free space ≥500 GB (${avail}G)"
  elif [[ -n "${avail:-}" ]] && [[ "$avail" -ge 200 ]]; then
    warn "free space ${avail}G — fetch may need ~600 GB"
  else
    bad "free space ${avail:-?}G — need ~600 GB"
  fi
else
  warn "could not measure disk"
fi

echo "-- memory --"
if [[ -r /proc/meminfo ]]; then
  mem_kb=$(awk '/MemTotal/{print $2}' /proc/meminfo)
  mem_gb=$((mem_kb / 1024 / 1024))
  if [[ "$mem_gb" -ge 32 ]]; then ok "RAM ${mem_gb} GB"
  elif [[ "$mem_gb" -ge 16 ]]; then warn "RAM ${mem_gb} GB — 64 GB recommended"
  else bad "RAM ${mem_gb} GB — too low"; fi
else warn "no /proc/meminfo"; fi

echo "-- cpu --"
cores=$(nproc 2>/dev/null || echo 1)
if [[ "$cores" -ge 8 ]]; then ok "CPU cores $cores"; else warn "CPU cores $cores"; fi

echo "-- tools --"
for cmd in git python3 curl; do
  if command -v "$cmd" >/dev/null 2>&1; then ok "$cmd"; else bad "$cmd missing"; fi
done
if command -v fetch >/dev/null 2>&1; then ok "depot_tools fetch on PATH"
elif [[ -x "${DEPOT_TOOLS:-}/fetch" ]]; then ok "depot_tools at DEPOT_TOOLS"
else bad "depot_tools not on PATH"; fi

echo "-- network --"
if curl -sS -o /dev/null --max-time 10 https://chromium.googlesource.com/ 2>/dev/null; then
  ok "chromium.googlesource.com"
else bad "cannot reach chromium.googlesource.com"; fi

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
if [[ -f "$ROOT/scripts/build-apk.sh" ]]; then ok "overlay at $ROOT"; else bad "overlay missing"; fi

echo
echo "=== summary: OK=$PASS WARN=$WARN FAIL=$FAIL ==="
if [[ "$FAIL" -gt 0 ]]; then echo "Runner NOT ready"; exit 1; fi
echo "Runner OK to proceed"
exit 0
