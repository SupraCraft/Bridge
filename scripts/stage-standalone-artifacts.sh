#!/usr/bin/env bash
set -euo pipefail

version="${1:?Bridge version is required}"
dest="${2:-dist}"

rm -rf "$dest"
mkdir -p "$dest"

stage_jar() {
  local module="$1"
  local source="build/${module}/${module}-${version}.jar"
  local target="${dest}/supracraft-${module}-${version}.jar"
  test -s "$source"
  cp "$source" "$target"
  cmp "$source" "$target"
}

stage_jar bridge
stage_jar bridge-asm
stage_jar bridge-plugin

test -s target/bridge-sbom.json
cp target/bridge-sbom.json "${dest}/supracraft-bridge-sbom-${version}.json"

printf '%s\n' \
  "${dest}/supracraft-bridge-${version}.jar" \
  "${dest}/supracraft-bridge-asm-${version}.jar" \
  "${dest}/supracraft-bridge-plugin-${version}.jar" \
  "${dest}/supracraft-bridge-sbom-${version}.json"
