#!/usr/bin/env bash
set -euo pipefail

required=(
  README.md
  AGENTS.md
  PROJECT_CONTRACT.json
  ARTIFACT_IDENTITY.md
  VERSIONING.md
  docs/DOCUMENTATION_POLICY.md
  docs/artifact-consumption.md
)
for path in "${required[@]}"; do
  test -s "$path" || { echo "Missing required documentation surface: $path" >&2; exit 1; }
done

python3 - <<'PY'
import json
import re
import xml.etree.ElementTree as ET
from pathlib import Path

contract = json.loads(Path('PROJECT_CONTRACT.json').read_text(encoding='utf-8'))
assert contract['repository'] == 'SupraCraft/Bridge'
assert contract['artifact']['group'] == 'io.github.supracraft.bridge'
assert contract['validation']['publication_model'] == 'build-once-promote-tested-bytes'
assert contract['provenance']['published_maven_bytes_are_tested_bytes'] is True

root = ET.parse('pom.xml').getroot()
ns = {'m': 'http://maven.apache.org/POM/4.0.0'}
def text(path):
    node = root.find(path, ns)
    assert node is not None and node.text
    return node.text.strip()

assert contract['source_version'] == text('m:version')
assert contract['artifact']['group'] == text('m:groupId')
assert int(contract['toolchain']['java_bytecode_release']) == int(text('m:properties/m:maven.compiler.release'))

wrapper = Path('.mvn/wrapper/maven-wrapper.properties').read_text(encoding='utf-8')
match = re.search(r'apache-maven-([0-9.]+)-bin', wrapper)
assert match, 'Unable to determine Maven version from wrapper properties'
assert contract['toolchain']['maven'] == match.group(1)
PY

active_docs=(
  README.md
  AGENTS.md
  PROJECT_CONTRACT.json
  docs/artifact-consumption.md
)

if grep -nHE '0\.1\.0-dev\.[0-9]+' "${active_docs[@]}"; then
  echo 'Volatile concrete Bridge development coordinate reappeared in evergreen documentation.' >&2
  exit 1
fi

if grep -nH -F 'mvn -P minecraft-it' "${active_docs[@]}"; then
  echo 'Unpinned system-Maven integration command reappeared in evergreen documentation.' >&2
  exit 1
fi

if grep -nH -F '# Bridge ASM' "${active_docs[@]}"; then
  echo 'Obsolete repository-level Bridge ASM title reappeared in evergreen documentation.' >&2
  exit 1
fi

printf 'Documentation contract OK\n'
