#!/usr/bin/env bash
# =============================================================================
# check-ci-leak.sh
# Verifies that no CI-only keys from ci-overrides.properties have leaked
# into the debug APK or any compiled build output.
#
# Usage:
#   ./scripts/check-ci-leak.sh
#
# Exit codes:
#   0 — no leaks found (success)
#   1 — one or more CI keys found in the binary (error)
#
# Run this after assembleDebug, e.g.:
#   ./gradlew assembleDebug && ./scripts/check-ci-leak.sh
# =============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
CI_OVERRIDES="$ROOT_DIR/ci-overrides.properties"

# --- Colours -----------------------------------------------------------------
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BOLD='\033[1m'
RESET='\033[0m'

echo ""
echo -e "${BOLD}=== CI Credential Leak Check ===${RESET}"
echo ""

# --- Sanity: ci-overrides must exist -----------------------------------------
if [[ ! -f "$CI_OVERRIDES" ]]; then
  echo -e "${YELLOW}⚠️  ci-overrides.properties not found at $CI_OVERRIDES${RESET}"
  echo "   Skipping check (nothing to compare against)."
  exit 0
fi

# --- Collect CI-only KEY names and their VALUES ------------------------------
declare -A CI_KEYS

while IFS= read -r line; do
  # Skip comments and blank lines
  [[ "$line" =~ ^#.*$ || -z "$line" ]] && continue

  key="${line%%=*}"
  value="${line#*=}"

  # Skip keys with empty values (nothing to search for)
  [[ -z "$value" ]] && continue

  CI_KEYS["$key"]="$value"
done < "$CI_OVERRIDES"

if [[ ${#CI_KEYS[@]} -eq 0 ]]; then
  echo -e "${YELLOW}⚠️  No non-empty values found in ci-overrides.properties — nothing to check.${RESET}"
  exit 0
fi

# --- Find the debug APK ------------------------------------------------------
APK_PATH=$(find "$ROOT_DIR" -path "*/build/outputs/apk/debug/*.apk" | head -1)

if [[ -z "$APK_PATH" ]]; then
  echo -e "${YELLOW}⚠️  No debug APK found. Run './gradlew assembleDebug' first.${RESET}"
  exit 1
fi

echo -e "Scanning APK: ${BOLD}$APK_PATH${RESET}"
echo ""

# --- Unzip APK to a temp dir -------------------------------------------------
TMP_DIR=$(mktemp -d)
trap 'rm -rf "$TMP_DIR"' EXIT

unzip -q "$APK_PATH" -d "$TMP_DIR"

# Also scan BuildConfig class files and any raw strings in the build folder
BUILD_SCAN_DIR="$ROOT_DIR/app/build"

# --- Check each CI key value -------------------------------------------------
LEAK_FOUND=0
CHECKED=0

for key in "${!CI_KEYS[@]}"; do
  value="${CI_KEYS[$key]}"
  CHECKED=$((CHECKED + 1))

  # Search inside the unzipped APK (binary + dex + resources)
  APK_HIT=$(grep -rl "$value" "$TMP_DIR" 2>/dev/null || true)

  # Search inside compiled build outputs (BuildConfig.java/.class, R files etc.)
  BUILD_HIT=$(grep -rl "$value" "$BUILD_SCAN_DIR" 2>/dev/null || true)

  if [[ -n "$APK_HIT" || -n "$BUILD_HIT" ]]; then
    LEAK_FOUND=$((LEAK_FOUND + 1))
    echo -e "${RED}❌ LEAK: ${BOLD}$key${RESET}${RED} found in binary!${RESET}"
    if [[ -n "$APK_HIT" ]]; then
      echo "   APK locations:"
      echo "$APK_HIT" | sed 's/^/     /'
    fi
    if [[ -n "$BUILD_HIT" ]]; then
      echo "   Build output locations:"
      echo "$BUILD_HIT" | sed 's/^/     /'
    fi
    echo ""
  else
    echo -e "${GREEN}✅ OK:  ${key}${RESET}"
  fi
done

# --- Summary -----------------------------------------------------------------
echo ""
echo -e "${BOLD}--- Summary ---${RESET}"
echo "   Keys checked : $CHECKED"

if [[ $LEAK_FOUND -gt 0 ]]; then
  echo -e "   ${RED}${BOLD}Leaks found  : $LEAK_FOUND${RESET}"
  echo ""
  echo -e "${RED}${BOLD}FAILED — CI credentials are present in the app binary.${RESET}"
  echo "   These keys must never reach BuildConfig. Check DependenciesPlugin.kt"
  echo "   and ensure ci-overrides.properties is not loaded via the secrets plugin."
  echo ""
  exit 1
else
  echo -e "   ${GREEN}${BOLD}Leaks found  : 0${RESET}"
  echo ""
  echo -e "${GREEN}${BOLD}PASSED — No CI credentials found in the app binary.${RESET}"
  echo ""
  exit 0
fi
