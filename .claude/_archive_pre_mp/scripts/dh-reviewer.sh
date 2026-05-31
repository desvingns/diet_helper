#!/usr/bin/env bash
# dh-reviewer.sh — Clean Architecture layer-boundary checks for diet_helper.
# Emits exactly one JSON line on stdout. No prose, no progress output.
#
# Usage:
#   dh-reviewer.sh <file1> <file2> ...
#   echo -e "file1\nfile2" | dh-reviewer.sh
#
# Paths in CHANGED_FILES are relative to repo root. Pre-existing violations in
# files NOT listed are ignored, per the agent contract.
#
# Output (clear):    {"pass":true,"violations":[]}
# Output (failure):  {"pass":false,"violations":["<path>:<line> — <msg>", ...]}

set -uo pipefail

PACKAGE="com.k.shavrin.diethelper"
SRC_ROOT="app/src/main/java/com/k/shavrin/diethelper"

REPO_ROOT=$(git rev-parse --show-toplevel 2>/dev/null) || {
  printf '{"pass":false,"violations":["not a git repo"]}\n'
  exit 0
}
cd "$REPO_ROOT"

# ----- collect CHANGED_FILES from args, or stdin if no args -------------
CHANGED=()
if [ "$#" -gt 0 ]; then
  for f in "$@"; do
    [ -n "$f" ] && CHANGED+=("$f")
  done
else
  # Only read stdin when no args given AND stdin is a pipe (not a TTY,
  # not the default closed stdin). Avoids hanging when invoked tool-side.
  if [ -p /dev/stdin ]; then
    while IFS= read -r line; do
      [ -n "$line" ] && CHANGED+=("$line")
    done
  fi
fi

# ----- JSON helpers ------------------------------------------------------
json_escape() {
  local s="$1"
  s="${s//\\/\\\\}"
  s="${s//\"/\\\"}"
  s="${s//$'\n'/\\n}"
  s="${s//$'\r'/\\r}"
  s="${s//$'\t'/\\t}"
  printf '%s' "$s"
}

VIOLATIONS=()
add_v() { VIOLATIONS+=("$1"); }

# ----- Filter CHANGED to existing files only ----------------------------
EXISTING=()
for f in "${CHANGED[@]:-}"; do
  [ -f "$f" ] && EXISTING+=("$f")
done

# Helper: does file path live under a given relative subpath?
under() {
  case "$1" in
    "$2"*) return 0 ;;
    *)     return 1 ;;
  esac
}

# ----- Check 1: no Android imports in domain --------------------------
for f in "${EXISTING[@]:-}"; do
  under "$f" "$SRC_ROOT/domain/" || continue
  while IFS=: read -r line _; do
    [ -z "$line" ] && continue
    offending=$(sed -n "${line}p" "$f" | sed 's/^[[:space:]]*//')
    add_v "$f:$line — illegal Android import in domain: $offending"
  done < <(grep -nE "^import android\." "$f" || true)
done

# ----- Check 2: no data layer imports in presentation -----------------
for f in "${EXISTING[@]:-}"; do
  under "$f" "$SRC_ROOT/presentation/" || continue
  while IFS=: read -r line _; do
    [ -z "$line" ] && continue
    offending=$(sed -n "${line}p" "$f" | sed 's/^[[:space:]]*//')
    add_v "$f:$line — illegal data import in presentation: $offending"
  done < <(grep -nE "^import ${PACKAGE//./\\.}\.data\." "$f" || true)
done

# ----- Check 3: ViewModels must not inject Repository -----------------
# Heuristic: in *ViewModel.kt files under presentation/, flag lines containing
# `: \w+Repository` (a type annotation in a constructor parameter) — but ignore
# imports and comment lines.
for f in "${EXISTING[@]:-}"; do
  under "$f" "$SRC_ROOT/presentation/" || continue
  case "$(basename "$f")" in
    *ViewModel.kt) ;;
    *) continue ;;
  esac
  while IFS=: read -r line content; do
    [ -z "$line" ] && continue
    case "$content" in
      *import\ *|*//*|*\*\ *) continue ;;
    esac
    offending=$(printf '%s' "$content" | sed 's/^[[:space:]]*//')
    add_v "$f:$line — ViewModel injects Repository directly (must go via UseCase): $offending"
  done < <(grep -nE ":\s*[A-Z][A-Za-z0-9_]*Repository\b" "$f" || true)
done

# ----- Check 4: Screen composables expose <Name>Content() -------------
for f in "${EXISTING[@]:-}"; do
  under "$f" "$SRC_ROOT/presentation/" || continue
  base=$(basename "$f")
  case "$base" in
    *Screen.kt) ;;
    *) continue ;;
  esac
  if ! grep -qE "^[[:space:]]*(public[[:space:]]+)?fun[[:space:]]+[A-Z][A-Za-z0-9_]*Content[[:space:]]*\(" "$f"; then
    add_v "$f — missing public <Name>Content(...) composable; Screen wrappers must expose a testable Content body"
  fi
done

# ----- Emit JSON --------------------------------------------------------
if [ "${#VIOLATIONS[@]}" -eq 0 ]; then
  printf '{"pass":true,"violations":[]}\n'
else
  out='{"pass":false,"violations":['
  i=0
  for v in "${VIOLATIONS[@]}"; do
    [ "$i" -gt 0 ] && out+=','
    out+="\"$(json_escape "$v")\""
    i=$((i + 1))
  done
  out+=']}'
  printf '%s\n' "$out"
fi
