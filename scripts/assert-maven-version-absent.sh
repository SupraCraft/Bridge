#!/usr/bin/env bash
set -euo pipefail

version="${1:?Bridge version is required}"
owner="${BRIDGE_OWNER:-${GITHUB_REPOSITORY_OWNER:-SupraCraft}}"
token="${GITHUB_TOKEN:-${GH_TOKEN:-}}"
actor="${GITHUB_ACTOR:-token}"

if [[ -z "$token" ]]; then
  echo 'GITHUB_TOKEN (or GH_TOKEN) is required to inspect GitHub Packages.' >&2
  exit 2
fi

base="https://maven.pkg.github.com/${owner}/Bridge/io/github/supracraft/bridge"
artifacts=(bridge-parent bridge bridge-asm bridge-plugin bridge-test)

for artifact in "${artifacts[@]}"; do
  url="${base}/${artifact}/maven-metadata.xml"
  tmp="$(mktemp)"
  code="$(curl -sS -u "${actor}:${token}" -o "$tmp" -w '%{http_code}' "$url")"
  case "$code" in
    200)
      if VERSION="$version" python3 - "$tmp" <<'PY'
import os
import sys
import xml.etree.ElementTree as ET
root = ET.parse(sys.argv[1]).getroot()
wanted = os.environ['VERSION']
versions = [node.text.strip() for node in root.findall('./versioning/versions/version') if node.text]
raise SystemExit(0 if wanted in versions else 1)
PY
      then
        rm -f "$tmp"
        echo "Immutable Bridge coordinate already exists: ${artifact}:${version}" >&2
        exit 1
      fi
      ;;
    404)
      ;;
    *)
      echo "Unable to inspect ${artifact} Maven metadata: HTTP ${code}" >&2
      rm -f "$tmp"
      exit 1
      ;;
  esac
  rm -f "$tmp"
done

printf 'Bridge Maven version is unused across all publication modules: %s\n' "$version"
