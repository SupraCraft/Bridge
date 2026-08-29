#!/usr/bin/env python3
"""Build Bridge's end-user/developer Pages surfaces from repository contracts."""

import argparse
import html
import json
import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DOCS = ROOT / "docs"


def load(path):
    return json.loads(Path(path).read_text(encoding="utf-8"))


def write_json(path, value):
    path = Path(path); path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2, sort_keys=True) + "\n", encoding="utf-8")


def write(path, value):
    path = Path(path); path.parent.mkdir(parents=True, exist_ok=True); path.write_text(value, encoding="utf-8")


def replace_fact(page, pattern, value, label):
    rendered, count = re.subn(pattern, lambda m: f"{m.group(1)}{value}{m.group(2)}", page, count=1)
    if count != 1: raise RuntimeError(f"unable to render {label}")
    return rendered


def shell(title, body, base):
    return f'''<!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>{html.escape(title)} · Bridge</title><link rel="icon" href="{base}/assets/brand/icon.svg" type="image/svg+xml"><style>
:root{{font-family:Inter,ui-sans-serif,system-ui,-apple-system,BlinkMacSystemFont,"Segoe UI",sans-serif;color:#243F46;background:#F5F2EB;line-height:1.55}}*{{box-sizing:border-box}}body{{margin:0}}a{{color:#255F6A}}.shell{{max-width:980px;margin:auto;padding:0 24px}}header{{background:#102B33;color:#F2EEE5}}nav{{display:flex;justify-content:space-between;gap:18px;padding:18px 0;flex-wrap:wrap}}nav a{{color:#D8E2E3;text-decoration:none;font-weight:650}}main{{padding:42px 0 72px}}h1{{font-size:clamp(2.2rem,6vw,4rem);line-height:1}}.card{{background:#fff;border:1px solid #D9D5CD;border-radius:14px;padding:20px;margin:18px 0}}.button{{display:inline-block;background:#C27742;color:#161D1F;border-radius:9px;padding:11px 15px;text-decoration:none;font-weight:700}}code{{font-family:ui-monospace,SFMono-Regular,Consolas,monospace;background:#E9E5DC;padding:.1em .3em;border-radius:4px}}pre{{overflow:auto;background:#17292E;color:#F2EEE5;padding:16px;border-radius:10px}}table{{width:100%;border-collapse:collapse;background:#fff}}th,td{{text-align:left;padding:10px;border-bottom:1px solid #D9D5CD}}.dot{{display:inline-block;width:.72em;height:.72em;border-radius:50%;background:#278451;margin-right:.45em}}.quiet{{color:#5C686B}}footer{{border-top:1px solid #D9D5CD;padding:24px 0 40px;color:#657174}}</style></head><body><header><div class="shell"><nav><a href="{base}/">Bridge</a><span><a href="{base}/use/">Use Bridge</a> · <a href="{base}/compatibility/">Compatibility</a> · <a href="{base}/releases/">Releases</a></span></nav></div></header><main class="shell">{body}</main><footer><div class="shell">Bridge · SupraCraft · MPL-2.0</div></footer></body></html>'''


def main():
    parser = argparse.ArgumentParser(); parser.add_argument("--output", default="build/public-site"); args = parser.parse_args()
    output = (ROOT / args.output).resolve() if not Path(args.output).is_absolute() else Path(args.output)
    if output.exists(): shutil.rmtree(output)
    shutil.copytree(DOCS, output)

    contract = load(ROOT / "PROJECT_CONTRACT.json")
    compatibility = load(ROOT / "COMPATIBILITY.json")
    metadata = load(ROOT / "GITHUB_METADATA.json")
    brand = load(DOCS / "assets/brand/brand.json")
    stable = load(ROOT / "CURRENT_STABLE.json")
    base = metadata["homepage"].rstrip("/")

    page_path = output / "index.html"; page = page_path.read_text(encoding="utf-8")
    page = replace_fact(page, r'(<code id="source-version">)[^<]*(</code>)', contract["source_version"], "source version")
    page = replace_fact(page, r'(<span id="java-release">)[^<]*(</span>)', str(contract["toolchain"]["java_bytecode_release"]), "Java release")
    page = replace_fact(page, r'(<span id="asm-version">)[^<]*(</span>)', compatibility["asm"]["version"], "ASM version")
    page = replace_fact(page, r'(<span id="host-jvms">)[^<]*(</span>)', ", ".join(str(v) for v in compatibility["host_jvm"]["pull_request_blocking"]), "host JVMs")
    page_path.write_text(page, encoding="utf-8")

    write_json(output / "project.json", contract); write_json(output / "compatibility.json", compatibility); write_json(output / "github.json", metadata); write_json(output / "brand.json", brand)
    write_json(output / "releases/stable.json", stable); write(output / "releases/stable.txt", stable["version"] + "\n"); write(output / "releases/stable-url.txt", stable["artifacts"]["bridge"]["download_url"] + "\n")
    write_json(output / "artifacts.json", {"schema_version":"1.1.0","repository":contract["repository"],"source_version":contract["source_version"],"stable_release":stable,"artifact":contract["artifact"],"versioning":contract["versioning"],"provenance":contract["provenance"]})

    version = stable["version"]
    repo = stable["maven"]["repository"]
    maven = f'''<properties>\n  <bridge.version>{version}</bridge.version>\n</properties>\n<repositories>\n  <repository><id>bridge-github</id><url>{repo}</url></repository>\n</repositories>\n<pluginRepositories>\n  <pluginRepository><id>bridge-github</id><url>{repo}</url></pluginRepository>\n</pluginRepositories>\n<dependencies>\n  <dependency>\n    <groupId>io.github.supracraft.bridge</groupId>\n    <artifactId>bridge</artifactId>\n    <version>${{bridge.version}}</version>\n    <scope>provided</scope>\n  </dependency>\n</dependencies>\n<build><plugins><plugin>\n  <groupId>io.github.supracraft.bridge</groupId>\n  <artifactId>bridge-plugin</artifactId>\n  <version>${{bridge.version}}</version>\n  <executions><execution><goals><goal>bridge</goal></goals></execution></executions>\n</plugin></plugins></build>'''
    bash = f'''BRIDGE_VERSION=$(curl -fsSL {base}/releases/stable.txt)\necho "$BRIDGE_VERSION"'''
    ps = f'''$bridgeVersion = (Invoke-RestMethod '{base}/releases/stable.txt').Trim()\n$bridgeVersion'''
    use_body = f'''<h1>Use Bridge {html.escape(version)}</h1><p>The current stable release is <strong>{html.escape(version)}</strong>. Pin that exact version across Bridge modules and the Maven plugin.</p><div class="card"><h2>Maven</h2><pre>{html.escape(maven)}</pre><p>GitHub Packages requires authentication. In Actions use <code>GITHUB_TOKEN</code>; locally use a token with package read permission and the matching GitHub actor.</p></div><h2>Stable version lookup</h2><p>Bash:</p><pre>{html.escape(bash)}</pre><p>PowerShell:</p><pre>{html.escape(ps)}</pre><p>Structured metadata: <a href="{base}/releases/stable.json">stable.json</a>.</p>'''
    write(output / "use/index.html", shell("Use Bridge", use_body, base))

    host = ", ".join(str(v) for v in compatibility["host_jvm"]["pull_request_blocking"])
    classfiles = ", ".join(str(v) for v in compatibility["class_files"]["qualification_targets"])
    compat_body = f'''<h1>Compatibility</h1><p>Bridge support claims are evidence-backed. Stable {html.escape(version)} passed the release qualification associated with its published release.</p><table><tr><th>State</th><th>Area</th><th>Qualified envelope</th></tr><tr><td><span class="dot"></span>Qualified</td><td>Host JVMs</td><td>{html.escape(host)}</td></tr><tr><td><span class="dot"></span>Qualified policy</td><td>Class-file inputs</td><td>Java {html.escape(classfiles)}</td></tr><tr><td><span class="dot"></span>Qualified</td><td>Product bytecode baseline</td><td>Java {contract['toolchain']['java_bytecode_release']}</td></tr></table><p>For exact machine policy and release evidence, see <a href="{base}/compatibility.json">compatibility.json</a> and the <a href="{stable['qualification']}">release qualification evidence</a>.</p>'''
    write(output / "compatibility/index.html", shell("Compatibility", compat_body, base))

    rows = ''.join(f'''<tr><td><code>{html.escape(name)}</code></td><td><a href="{html.escape(item['download_url'])}">Download</a></td><td><code>{html.escape(item['sha256'])}</code></td></tr>''' for name,item in stable["artifacts"].items())
    release_body = f'''<h1>Bridge {html.escape(version)}</h1><p>{html.escape(stable['summary'])}</p><div class="card"><h2>Recommended consumption</h2><p>Use the canonical Maven coordinates shown on the <a href="{base}/use/">Use Bridge</a> page. Standalone release JARs are available below for inspection or tooling that needs them directly.</p></div><table><tr><th>Artifact</th><th>Download</th><th>SHA-256</th></tr>{rows}</table><p><a href="{stable['qualification']}">Qualification evidence</a></p>'''
    write(output / f"releases/{version}/index.html", shell(f"Release {version}", release_body, base))
    write(output / "releases/index.html", shell("Releases", f'''<h1>Bridge releases</h1><p><a href="{base}/releases/{html.escape(version)}/">Bridge {html.escape(version)}</a> — current stable</p><p>GitHub remains the source/contribution record; usage and release information stay on this site.</p>''', base))

    write(output / "llms.txt", f'''# Bridge\n\nCanonical human entry point: {base}/\nUse Bridge: {base}/use/\nCompatibility: {base}/compatibility/\nCurrent stable JSON: {base}/releases/stable.json\nCurrent stable version: {base}/releases/stable.txt\nCurrent stable primary artifact URL: {base}/releases/stable-url.txt\nProject contract: {base}/project.json\nCompatibility policy: {base}/compatibility.json\nRepository: https://github.com/{contract['repository']}\nUpstream: https://github.com/{contract['upstream_repository']}\n\nPrefer Pages endpoints over scraping GitHub release pages.\n''')


if __name__ == "__main__": main()
