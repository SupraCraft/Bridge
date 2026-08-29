#!/usr/bin/env bash
set -euo pipefail

required=(
  README.md
  AGENTS.md
  PROJECT_CONTRACT.json
  COMPATIBILITY.json
  BRAND_PROFILE.json
  GITHUB_METADATA.json
  ARTIFACT_IDENTITY.md
  VERSIONING.md
  docs/DOCUMENTATION_POLICY.md
  docs/artifact-consumption.md
  docs/diagnostics.md
  docs/index.html
  docs/roadmap/0.1.0-uplift.md
  docs/brand-guidelines.md
  docs/assets/brand/icon.svg
  docs/assets/brand/hero.svg
  docs/assets/brand/brand.json
  schemas/bridge-run-report.schema.json
  bridge/resources/META-INF/supracraft/bridge/icon.svg
  scripts/apply-github-metadata.py
  scripts/build-public-site.py
  scripts/check-public-site.py
  scripts/check-run-report.py
  .github/workflows/compatibility.yml
  .github/workflows/pages.yml
)
for path in "${required[@]}"; do
  test -s "$path" || { echo "Missing required documentation/public surface: $path" >&2; exit 1; }
done

cmp docs/assets/brand/icon.svg bridge/resources/META-INF/supracraft/bridge/icon.svg

python3 - <<'PY'
import json
import re
import tempfile
import xml.etree.ElementTree as ET
from pathlib import Path
import subprocess

contract = json.loads(Path('PROJECT_CONTRACT.json').read_text(encoding='utf-8'))
compatibility = json.loads(Path('COMPATIBILITY.json').read_text(encoding='utf-8'))
profile = json.loads(Path('BRAND_PROFILE.json').read_text(encoding='utf-8'))
metadata = json.loads(Path('GITHUB_METADATA.json').read_text(encoding='utf-8'))
brand = json.loads(Path('docs/assets/brand/brand.json').read_text(encoding='utf-8'))
report_schema = json.loads(Path('schemas/bridge-run-report.schema.json').read_text(encoding='utf-8'))
page = Path('docs/index.html').read_text(encoding='utf-8')
pages_workflow = Path('.github/workflows/pages.yml').read_text(encoding='utf-8')
compatibility_workflow = Path('.github/workflows/compatibility.yml').read_text(encoding='utf-8')

assert contract['repository'] == 'SupraCraft/Bridge'
assert contract['artifact']['group'] == 'io.github.supracraft.bridge'
assert contract['artifact']['embedded_project_icon'] == 'bridge:META-INF/supracraft/bridge/icon.svg'
assert contract['brand']['organization'] == profile['organization_brand'] == 'SupraCraft'
assert contract['brand']['contract_version'] == profile['brand_contract_version'] == brand['organization_brand']['contract_version']
assert contract['brand']['profile'] == contract['public_surface']['brand_profile'] == 'BRAND_PROFILE.json'
assert contract['brand']['runtime_dependency_on_private_repo'] is False
assert profile['snapshot_policy'] == 'vendored-reviewed-snapshot-no-private-runtime-dependency'
assert profile['identity']['minecraft_specific'] is False
assert profile['packaged_resources']['source_path'] == 'bridge/resources/META-INF/supracraft/bridge/icon.svg'
assert contract['validation']['publication_model'] == 'build-once-promote-tested-bytes'
assert contract['validation']['public_site_builder'] == 'scripts/build-public-site.py'
assert contract['validation']['public_site_check'] == 'scripts/check-public-site.py'
assert contract['validation']['compatibility_workflow'] == '.github/workflows/compatibility.yml'
assert contract['validation']['run_report_validator'] == 'scripts/check-run-report.py'
assert contract['compatibility']['contract'] == 'COMPATIBILITY.json'
assert contract['compatibility']['workflow'] == '.github/workflows/compatibility.yml'
assert contract['compatibility']['support_claims_require_evidence'] is True
assert contract['compatibility']['one_bridge_build_per_bridge_version'] is True
assert contract['provenance']['published_maven_bytes_are_tested_bytes'] is True
assert contract['public_surface']['github_metadata'] == 'GITHUB_METADATA.json'
assert contract['public_surface']['metadata_apply'] == 'scripts/apply-github-metadata.py'
assert contract['public_surface']['pages_entrypoint'] == 'docs/index.html'
assert contract['public_surface']['pages_source'] == 'github-actions'
assert contract['public_surface']['pages_workflow'] == '.github/workflows/pages.yml'
assert contract['public_surface']['site_builder'] == 'scripts/build-public-site.py'
assert contract['public_surface']['brand_manifest'] == 'docs/assets/brand/brand.json'
assert contract['public_surface']['pages_url'] == metadata['homepage'] == metadata['pages']['url']
assert 'compatibility.json' in contract['public_surface']['machine_endpoints']
assert metadata['repository'] == contract['repository']
assert metadata['upstream_repository'] == contract['upstream_repository']
assert metadata['pages']['expected_enabled'] is True
assert metadata['pages']['source'] == 'github-actions'
assert metadata['pages']['builder'] == 'scripts/build-public-site.py'
assert metadata['pages']['workflow'] == '.github/workflows/pages.yml'
assert metadata['topics'] == sorted(set(metadata['topics']))
assert brand['project'] == 'Bridge'
assert brand['identity'] == 'Java bytecode tooling'
assert brand['organization_brand']['profile_snapshot'] == 'BRAND_PROFILE.json'
assert brand['organization_brand']['runtime_dependency_on_private_repo'] is False
assert 'ME1312/Bridge' in page
assert 'io.github.supracraft.bridge' in page
assert metadata['homepage'] in page
assert metadata['description'] in page
assert 'Minecraft' not in brand['identity']
assert 'href="compatibility.json"' in page
assert 'scripts/check-public-site.py' in pages_workflow
assert '--site-dir build/public-site' in pages_workflow
assert '--base-url "${{ steps.deployment.outputs.page_url }}"' in pages_workflow

run_report = contract['diagnostics']['structured_run_report']
assert contract['diagnostics']['human_transport'] == 'maven-log'
assert contract['diagnostics']['stable_diagnostic_ids'] is True
assert contract['diagnostics']['authoritative_failure_signal'] == 'maven-process-exit-code'
assert run_report['enabled_by_default'] is True
assert run_report['default_path'] == '${project.build.directory}/bridge/bridge-report.json'
assert run_report['schema_id'] == 'bridge-run/1'
assert run_report['schema'] == 'schemas/bridge-run-report.schema.json'
assert run_report['validator'] == 'scripts/check-run-report.py'
assert run_report['artifact_identity'] is False
assert run_report['contains_absolute_environment_paths'] is False
assert report_schema['properties']['schema']['const'] == run_report['schema_id']
assert 'python3 scripts/check-run-report.py build/bridge-test/bridge/bridge-report.json' in compatibility_workflow

root = ET.parse('pom.xml').getroot()
ns = {'m': 'http://maven.apache.org/POM/4.0.0'}
def text(path):
    node = root.find(path, ns)
    assert node is not None and node.text
    return node.text.strip()

assert contract['source_version'] == text('m:version')
assert contract['artifact']['group'] == text('m:groupId')
assert int(contract['toolchain']['java_bytecode_release']) == int(text('m:properties/m:maven.compiler.release'))
assert contract['toolchain']['asm'] == text('m:properties/m:asm.version')
assert compatibility['repository'] == contract['repository']
assert compatibility['bridge_source_version'] == contract['source_version']
assert compatibility['host_jvm']['minimum'] == contract['toolchain']['java_bytecode_release']
assert compatibility['asm']['version'] == contract['toolchain']['asm']
assert compatibility['asm']['single_version_required'] is True
assert compatibility['maven']['canonical'] == contract['toolchain']['maven']
assert compatibility['host_jvm']['pull_request_blocking'] == [21, 25, 26]
assert compatibility['host_jvm']['advisory_early_access'] == ['27-ea']

for module in ('bridge-asm', 'bridge-plugin'):
    module_root = ET.parse(f'{module}/pom.xml').getroot()
    versions = []
    for dep in module_root.findall('m:dependencies/m:dependency', ns):
        group = dep.find('m:groupId', ns)
        version = dep.find('m:version', ns)
        if group is not None and group.text.strip() == 'org.ow2.asm':
            assert version is not None and version.text.strip() == '${asm.version}', f'{module} must use parent asm.version'
            versions.append(version.text.strip())
    assert versions, f'{module} must declare an ASM dependency'

for host in compatibility['host_jvm']['pull_request_blocking']:
    assert f"'{host}'" in compatibility_workflow, f'missing blocking host JDK {host} in compatibility workflow'
assert '27-ea' in compatibility_workflow
assert 'continue-on-error: true' in compatibility_workflow

wrapper = Path('.mvn/wrapper/maven-wrapper.properties').read_text(encoding='utf-8')
match = re.search(r'apache-maven-([0-9.]+)-bin', wrapper)
assert match, 'Unable to determine Maven version from wrapper properties'
assert contract['toolchain']['maven'] == match.group(1)

with tempfile.TemporaryDirectory() as tmp:
    subprocess.run(['python3', 'scripts/build-public-site.py', '--output', tmp], check=True)
    subprocess.run(['python3', 'scripts/check-public-site.py', '--site-dir', tmp], check=True)
    out = Path(tmp)
    for name in contract['public_surface']['machine_endpoints']:
        assert (out / name).is_file(), f'missing generated endpoint: {name}'
    assert json.loads((out / 'project.json').read_text()) == contract
    assert json.loads((out / 'compatibility.json').read_text()) == compatibility
    assert json.loads((out / 'github.json').read_text()) == metadata
    assert json.loads((out / 'brand.json').read_text()) == brand
PY

active_docs=(
  README.md
  AGENTS.md
  PROJECT_CONTRACT.json
  COMPATIBILITY.json
  BRAND_PROFILE.json
  GITHUB_METADATA.json
  docs/artifact-consumption.md
  docs/diagnostics.md
  docs/index.html
  docs/roadmap/0.1.0-uplift.md
  docs/brand-guidelines.md
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

printf 'Documentation/public-surface contract OK\n'
