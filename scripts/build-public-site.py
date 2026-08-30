#!/usr/bin/env python3
"""Build Bridge's end-user/developer Pages surfaces from repository contracts."""

import argparse
import html
import json
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DOCS = ROOT / "docs"
SOURCE_URL = "https://github.com/SupraCraft/Bridge"


def load(path):
    return json.loads(Path(path).read_text(encoding="utf-8"))


def write_json(path, value):
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2, sort_keys=True) + "\n", encoding="utf-8")


def write(path, value):
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(value, encoding="utf-8")


def join_url(base, path=""):
    base = base.rstrip("/")
    path = path.lstrip("/")
    return f"{base}/{path}" if path else f"{base}/"


def external_link(url, label, css_class=""):
    classes = " ".join(item for item in (css_class, "external-link") if item)
    return (
        f'<a class="{html.escape(classes, quote=True)}" '
        f'href="{html.escape(url, quote=True)}" target="_blank" rel="noopener noreferrer">'
        f'{html.escape(label)} '
        '<span class="external-link-indicator" aria-hidden="true">↗</span>'
        '<span class="sr-only"> (opens in a new tab or window)</span>'
        '</a>'
    )


def shell(title, description, body, route, base, canonical_base):
    nav = [
        ("use/", "Use Bridge"),
        ("compatibility/", "Compatibility"),
        ("releases/", "Releases"),
        ("accessibility/", "Accessibility"),
    ]
    links = []
    for path, label in nav:
        is_current = route == path or (path == "releases/" and route.startswith("releases/"))
        current = ' aria-current="page"' if is_current else ""
        links.append(f'<a href="{html.escape(join_url(base, path))}"{current}>{html.escape(label)}</a>')
    brand_current = ' aria-current="page"' if route == "" else ""
    canonical = join_url(canonical_base, route)
    source = external_link(SOURCE_URL, "Source")
    return f'''<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <meta name="description" content="{html.escape(description)}">
  <meta name="color-scheme" content="light dark">
  <meta name="theme-color" content="#E0ECEC" data-effective-theme="light">
  <link rel="canonical" href="{html.escape(canonical)}">
  <link rel="icon" href="{html.escape(join_url(base, 'assets/brand/icon.svg'))}" type="image/svg+xml">
  <link rel="stylesheet" href="{html.escape(join_url(base, 'assets/site.css'))}">
  <title>{html.escape(title)} · Bridge</title>
  <script src="{html.escape(join_url(base, 'assets/site.js'))}" defer></script>
</head>
<body>
<a class="skip-link" href="#main-content">Skip to main content</a>
<header class="site-header">
  <div class="shell">
    <nav class="site-nav" aria-label="Primary">
      <a class="brand" href="{html.escape(join_url(base))}"{brand_current}>
        <img class="brand-mark" src="{html.escape(join_url(base, 'assets/brand/icon.svg'))}" alt="">
        <span>Bridge</span>
      </a>
      <div class="nav-cluster">
        <div class="nav-links">{''.join(links)}</div>
        <label class="theme-control" for="theme-select">Theme
          <select id="theme-select" aria-label="Theme">
            <option value="system">System</option>
            <option value="light">Light</option>
            <option value="dark">Dark</option>
          </select>
        </label>
      </div>
    </nav>
  </div>
</header>
<main id="main-content" class="shell main" tabindex="-1">{body}</main>
<footer class="site-footer">
  <div class="shell footer-row">
    <span>Bridge · SupraCraft · MPL-2.0 · derived from ME1312/Bridge</span>
    <span><a href="{html.escape(join_url(base, 'accessibility/'))}">Accessibility</a> · {source}</span>
  </div>
</footer>
</body>
</html>
'''


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--output", default="build/public-site")
    parser.add_argument("--base-url")
    args = parser.parse_args()

    output = (ROOT / args.output).resolve() if not Path(args.output).is_absolute() else Path(args.output)
    if output.exists():
        shutil.rmtree(output)
    shutil.copytree(DOCS, output)

    contract = load(ROOT / "PROJECT_CONTRACT.json")
    compatibility = load(ROOT / "COMPATIBILITY.json")
    metadata = load(ROOT / "GITHUB_METADATA.json")
    brand = load(DOCS / "assets/brand/brand.json")
    stable = load(ROOT / "CURRENT_STABLE.json")
    canonical_base = metadata["homepage"].rstrip("/")
    base = (args.base_url or canonical_base).rstrip("/")
    version = stable["version"]

    index_body = f'''
<section class="hero">
  <div>
    <div class="eyebrow">Java · Maven · bytecode</div>
    <h1>Bridge</h1>
    <p>{html.escape(metadata['description'])}</p>
    <div class="actions">
      <a class="button primary" href="{html.escape(join_url(base, 'use/'))}">Use Bridge {html.escape(version)}</a>
      <a class="button" href="{html.escape(join_url(base, 'compatibility/'))}">Check compatibility</a>
    </div>
  </div>
  <img class="hero-art" src="{html.escape(join_url(base, 'assets/brand/hero.svg'))}" alt="Source and class-file panels connected by a modern bridge.">
</section>
<section class="facts" aria-label="Bridge facts">
  <div class="fact"><strong>Maven group</strong><br><code>{html.escape(contract['artifact']['group'])}</code></div>
  <div class="fact"><strong>Current stable</strong><br><code>{html.escape(version)}</code></div>
  <div class="fact"><strong>Source line</strong><br><code>{html.escape(contract['source_version'])}</code></div>
  <div class="fact"><strong>Java bytecode baseline</strong><br>{contract['toolchain']['java_bytecode_release']}</div>
  <div class="fact"><strong>ASM baseline</strong><br>{html.escape(compatibility['asm']['version'])}</div>
</section>
<section class="section cards">
  <article class="card"><h2>Use Bridge</h2><p>Copy the canonical Maven configuration for the current stable release and understand package authentication.</p><p><a href="{html.escape(join_url(base, 'use/'))}">Open the usage guide →</a></p></article>
  <article class="card"><h2>Compatibility</h2><p>Read the tested host-JVM and class-file envelope backed by release qualification evidence.</p><p><a href="{html.escape(join_url(base, 'compatibility/'))}">View compatibility →</a></p></article>
  <article class="card"><h2>Releases</h2><p>Get version-specific artifact links, checksums, and qualification evidence without navigating the repository UI.</p><p><a href="{html.escape(join_url(base, 'releases/'))}">Browse releases →</a></p></article>
</section>
<section class="section machine">
  <h2>For scripts and automation</h2>
  <p class="quiet">These are stable public interfaces. Prefer them over scraping GitHub release pages.</p>
  <div class="link-list">
    <a href="{html.escape(join_url(base, 'project.json'))}">project.json</a>
    <a href="{html.escape(join_url(base, 'compatibility.json'))}">compatibility.json</a>
    <a href="{html.escape(join_url(base, 'github.json'))}">github.json</a>
    <a href="{html.escape(join_url(base, 'brand.json'))}">brand.json</a>
    <a href="{html.escape(join_url(base, 'artifacts.json'))}">artifacts.json</a>
    <a href="{html.escape(join_url(base, 'releases/stable.json'))}">stable.json</a>
    <a href="{html.escape(join_url(base, 'releases/stable.txt'))}">stable.txt</a>
    <a href="{html.escape(join_url(base, 'llms.txt'))}">llms.txt</a>
  </div>
</section>
'''
    write(output / "index.html", shell("Java bytecode tooling", metadata["description"], index_body, "", base, canonical_base))

    write_json(output / "project.json", contract)
    write_json(output / "compatibility.json", compatibility)
    write_json(output / "github.json", metadata)
    write_json(output / "brand.json", brand)
    write_json(output / "releases/stable.json", stable)
    write(output / "releases/stable.txt", version + "\n")
    write(output / "releases/stable-url.txt", stable["artifacts"]["bridge"]["download_url"] + "\n")
    write_json(output / "artifacts.json", {
        "schema_version": "1.1.0",
        "repository": contract["repository"],
        "source_version": contract["source_version"],
        "stable_release": stable,
        "artifact": contract["artifact"],
        "versioning": contract["versioning"],
        "provenance": contract["provenance"],
    })

    repo = stable["maven"]["repository"]
    maven = f'''<properties>\n  <bridge.version>{version}</bridge.version>\n</properties>\n<repositories>\n  <repository><id>bridge-github</id><url>{repo}</url></repository>\n</repositories>\n<pluginRepositories>\n  <pluginRepository><id>bridge-github</id><url>{repo}</url></pluginRepository>\n</pluginRepositories>\n<dependencies>\n  <dependency>\n    <groupId>io.github.supracraft.bridge</groupId>\n    <artifactId>bridge</artifactId>\n    <version>${{bridge.version}}</version>\n    <scope>provided</scope>\n  </dependency>\n</dependencies>\n<build><plugins><plugin>\n  <groupId>io.github.supracraft.bridge</groupId>\n  <artifactId>bridge-plugin</artifactId>\n  <version>${{bridge.version}}</version>\n  <executions><execution><goals><goal>bridge</goal></goals></execution></executions>\n</plugin></plugins></build>'''
    bash = f'''BRIDGE_VERSION=$(curl -fsSL {join_url(canonical_base, 'releases/stable.txt')})\necho "$BRIDGE_VERSION"'''
    ps = f'''$bridgeVersion = (Invoke-RestMethod '{join_url(canonical_base, 'releases/stable.txt')}').Trim()\n$bridgeVersion'''
    use_body = f'''
<h1>Use Bridge {html.escape(version)}</h1>
<p>The current stable release is <strong>{html.escape(version)}</strong>. Pin that exact version across Bridge modules and the Maven plugin.</p>
<div class="notice"><strong>GitHub Packages requires authentication.</strong> In GitHub Actions use <code>GITHUB_TOKEN</code>; locally use a token with package read permission and the matching GitHub actor.</div>
<section class="section"><h2>Maven</h2><pre><code>{html.escape(maven)}</code></pre></section>
<section class="section"><h2>Stable version lookup</h2><p>Bash:</p><pre><code>{html.escape(bash)}</code></pre><p>PowerShell:</p><pre><code>{html.escape(ps)}</code></pre><p>Structured metadata: <a href="{html.escape(join_url(base, 'releases/stable.json'))}">stable.json</a>.</p></section>
'''
    write(output / "use/index.html", shell(f"Use Bridge {version}", "Use the current stable Bridge Maven coordinates and lookup endpoints.", use_body, "use/", base, canonical_base))

    host = ", ".join(str(v) for v in compatibility["host_jvm"]["pull_request_blocking"])
    classfiles = ", ".join(str(v) for v in compatibility["class_files"]["qualification_targets"])
    qualification = external_link(stable["qualification"], "release qualification evidence")
    compat_body = f'''
<h1>Compatibility</h1>
<p>Bridge support claims are evidence-backed. Stable {html.escape(version)} passed the release qualification associated with its published release.</p>
<div class="table-scroll"><table><caption>Qualified Bridge {html.escape(version)} compatibility envelope</caption><thead><tr><th scope="col">State</th><th scope="col">Area</th><th scope="col">Qualified envelope</th></tr></thead><tbody>
<tr><td><span class="status-dot status-pass" aria-hidden="true"></span>Qualified</td><td>Host JVMs</td><td>{html.escape(host)}</td></tr>
<tr><td><span class="status-dot status-pass" aria-hidden="true"></span>Qualified policy</td><td>Class-file inputs</td><td>Java {html.escape(classfiles)}</td></tr>
<tr><td><span class="status-dot status-pass" aria-hidden="true"></span>Qualified</td><td>Product bytecode baseline</td><td>Java {contract['toolchain']['java_bytecode_release']}</td></tr>
</tbody></table></div>
<p>For exact machine policy and release evidence, see <a href="{html.escape(join_url(base, 'compatibility.json'))}">compatibility.json</a> and the {qualification}.</p>
'''
    write(output / "compatibility/index.html", shell("Compatibility", "Evidence-backed Bridge JVM and class-file compatibility.", compat_body, "compatibility/", base, canonical_base))

    release_rows = ''.join(
        f'''<tr><th scope="row"><code>{html.escape(item['name'])}</code></th><td>{external_link(item['download_url'], 'Download')}</td><td><code>{html.escape(item['sha256'])}</code></td></tr>'''
        for item in stable["artifacts"].values()
    )
    release_body = f'''
<h1>Bridge {html.escape(version)}</h1>
<p>{html.escape(stable['summary'])}</p>
<div class="card"><h2>Recommended consumption</h2><p>Use the canonical Maven coordinates shown on the <a href="{html.escape(join_url(base, 'use/'))}">Use Bridge</a> page. Standalone JARs below are provided for inspection or tooling that needs direct artifacts.</p></div>
<div class="table-scroll"><table><caption>Bridge {html.escape(version)} standalone artifacts</caption><thead><tr><th scope="col">Artifact</th><th scope="col">Download</th><th scope="col">SHA-256</th></tr></thead><tbody>{release_rows}</tbody></table></div>
<p>{external_link(stable['qualification'], 'Qualification evidence')}</p>
'''
    write(output / f"releases/{version}/index.html", shell(f"Release {version}", f"Bridge {version} release artifacts, checksums, and qualification evidence.", release_body, f"releases/{version}/", base, canonical_base))

    releases_body = f'''
<h1>Bridge releases</h1>
<div class="card"><h2>Current stable</h2><p><a href="{html.escape(join_url(base, f'releases/{version}/'))}">Bridge {html.escape(version)}</a></p><p>{html.escape(stable['summary'])}</p></div>
<p>GitHub remains the source and contribution record; human-friendly usage and release information stays on this site. Use the secondary Source link in the footer when repository access is actually needed.</p>
'''
    write(output / "releases/index.html", shell("Releases", "Bridge stable releases and release evidence.", releases_body, "releases/", base, canonical_base))

    accessibility_body = f'''
<h1>Accessibility</h1>
<p>This public site targets <strong>WCAG 2.2 Level AA</strong> and is designed to align with the Revised Section 508 web accessibility criteria where applicable.</p>
<p>Automated testing is evidence, not certification. Candidate releases are checked with Playwright across desktop and mobile browser profiles, axe accessibility rules, Lighthouse quality budgets, deterministic contract checks, and link validation.</p>
<h2>Interaction and display</h2>
<ul><li>Keyboard-visible focus and a skip link are provided.</li><li>Primary navigation remains available at narrow widths.</li><li>Theme choices support System, Light, and Dark; explicit choices persist locally.</li><li>The layout supports reflow to 320 CSS pixels without intentional horizontal page scrolling.</li><li>Reduced-motion and forced-colors preferences are respected.</li><li>External links use a visible ↗ indicator plus an assistive-technology notification and preserve this site in the current browsing context.</li></ul>
<h2>Report a problem</h2>
<p>Bridge Issues are restricted to repository collaborators. Other users can use the secondary Source link in this page footer to reach the repository's available contribution/contact channels and report a reproducible accessibility problem.</p>
'''
    write(output / "accessibility/index.html", shell("Accessibility", "Bridge public-site accessibility target, testing posture, and interaction support.", accessibility_body, "accessibility/", base, canonical_base))

    write(output / "llms.txt", f'''# Bridge\n\nCanonical human entry point: {join_url(canonical_base)}\nUse Bridge: {join_url(canonical_base, 'use/')}\nCompatibility: {join_url(canonical_base, 'compatibility/')}\nAccessibility: {join_url(canonical_base, 'accessibility/')}\nCurrent stable JSON: {join_url(canonical_base, 'releases/stable.json')}\nCurrent stable version: {join_url(canonical_base, 'releases/stable.txt')}\nCurrent stable primary artifact URL: {join_url(canonical_base, 'releases/stable-url.txt')}\nProject contract: {join_url(canonical_base, 'project.json')}\nCompatibility policy: {join_url(canonical_base, 'compatibility.json')}\nRepository: https://github.com/{contract['repository']}\nUpstream: https://github.com/{contract['upstream_repository']}\n\nPrefer Pages endpoints over scraping GitHub release pages.\n''')


if __name__ == "__main__":
    main()
