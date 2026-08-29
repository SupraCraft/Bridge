#!/usr/bin/env python3
"""Validate generated or deployed Bridge Pages surfaces."""

import argparse
import json
import time
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
REMOTE_ATTEMPTS = 30
REMOTE_DELAY_SECONDS = 5


def load_json(path): return json.loads(Path(path).read_text(encoding="utf-8"))


class SiteReader:
    def __init__(self, site_dir=None, base_url=None):
        self.site_dir = Path(site_dir).resolve() if site_dir else None
        self.base_url = base_url.rstrip("/") if base_url else None
        self.cache_key = None
    def begin_attempt(self, attempt):
        if self.base_url: self.cache_key = f"{int(time.time())}-{attempt}"
    def read_text(self, relative_path):
        if self.site_dir:
            path = self.site_dir / relative_path
            if not path.is_file(): raise AssertionError(f"missing public-site file: {relative_path}")
            return path.read_text(encoding="utf-8")
        url = f"{self.base_url}/{relative_path.lstrip('/')}"
        if self.cache_key: url += "?" + urllib.parse.urlencode({"supra_check": self.cache_key})
        request = urllib.request.Request(url, headers={"User-Agent":"SupraCraft-public-surface-check/2","Cache-Control":"no-cache"})
        with urllib.request.urlopen(request, timeout=20) as response:
            if response.status != 200: raise AssertionError(f"{url}: HTTP {response.status}")
            return response.read().decode("utf-8")


def validate_site(reader, contract, compatibility, metadata, expected_brand, stable):
    page = reader.read_text("index.html")
    assert '<html lang="en">' in page
    assert '<meta name="viewport"' in page
    assert f'<link rel="canonical" href="{metadata["homepage"]}">' in page
    assert metadata["description"] in page
    assert contract["upstream_repository"] in page
    assert 'href="assets/brand/icon.svg"' in page
    assert 'href="use/"' in page and 'href="compatibility/"' in page and 'href="releases/"' in page

    for asset in ("assets/brand/icon.svg", "assets/brand/hero.svg"):
        assert reader.read_text(asset).strip()
    for name in contract["public_surface"]["machine_endpoints"]:
        assert f'href="{name}"' in page
        assert reader.read_text(name).strip()
    for name in ("releases/stable.json", "releases/stable.txt", "releases/stable-url.txt"):
        assert f'href="{name}"' in page
        assert reader.read_text(name).strip()

    project = json.loads(reader.read_text("project.json")); github = json.loads(reader.read_text("github.json")); brand = json.loads(reader.read_text("brand.json")); artifacts = json.loads(reader.read_text("artifacts.json")); published_compatibility = json.loads(reader.read_text("compatibility.json")); stable_endpoint = json.loads(reader.read_text("releases/stable.json"))
    assert project == contract
    assert github == metadata
    assert brand == expected_brand
    assert published_compatibility == compatibility
    assert artifacts["repository"] == contract["repository"]
    assert artifacts["source_version"] == contract["source_version"]
    assert artifacts["artifact"] == contract["artifact"]
    assert artifacts["versioning"] == contract["versioning"]
    assert artifacts["provenance"] == contract["provenance"]
    assert artifacts["stable_release"] == stable
    assert stable_endpoint == stable
    assert reader.read_text("releases/stable.txt").strip() == stable["version"]
    assert reader.read_text("releases/stable-url.txt").strip() == stable["artifacts"]["bridge"]["download_url"]

    use = reader.read_text("use/index.html"); compatibility_page = reader.read_text("compatibility/index.html"); releases = reader.read_text("releases/index.html"); release = reader.read_text(f"releases/{stable['version']}/index.html")
    assert stable["version"] in use and stable["maven"]["group"] in use and stable["maven"]["repository"] in use
    assert "Host JVMs" in compatibility_page and "Class-file inputs" in compatibility_page
    assert stable["version"] in releases and stable["version"] in release
    assert stable["artifacts"]["bridge"]["download_url"] in release

    llms = reader.read_text("llms.txt"); base = metadata["homepage"].rstrip("/")
    assert f"Canonical human entry point: {base}/" in llms
    assert f"Compatibility policy: {base}/compatibility.json" in llms
    assert f"Current stable JSON: {base}/releases/stable.json" in llms
    assert f"Repository: https://github.com/{contract['repository']}" in llms
    assert f"Upstream: https://github.com/{contract['upstream_repository']}" in llms

    assert f'id="source-version">{contract["source_version"]}</code>' in page
    assert f'id="java-release">{contract["toolchain"]["java_bytecode_release"]}</span>' in page
    assert f'id="asm-version">{compatibility["asm"]["version"]}</span>' in page


def main():
    parser = argparse.ArgumentParser(); mode = parser.add_mutually_exclusive_group(required=True); mode.add_argument("--site-dir"); mode.add_argument("--base-url"); args = parser.parse_args()
    contract = load_json(ROOT/"PROJECT_CONTRACT.json"); compatibility = load_json(ROOT/"COMPATIBILITY.json"); metadata = load_json(ROOT/"GITHUB_METADATA.json"); brand = load_json(ROOT/"docs/assets/brand/brand.json"); stable = load_json(ROOT/"CURRENT_STABLE.json")
    reader = SiteReader(site_dir=args.site_dir, base_url=args.base_url)
    if args.site_dir:
        validate_site(reader, contract, compatibility, metadata, brand, stable)
    else:
        retryable = (AssertionError, json.JSONDecodeError, OSError, UnicodeError, urllib.error.HTTPError, urllib.error.URLError)
        for attempt in range(1, REMOTE_ATTEMPTS + 1):
            reader.begin_attempt(attempt)
            try:
                validate_site(reader, contract, compatibility, metadata, brand, stable); break
            except retryable as exc:
                if attempt == REMOTE_ATTEMPTS: raise AssertionError(f"deployed public-site contract did not converge after {REMOTE_ATTEMPTS} attempts: {exc}") from exc
                print(f"Public site not converged (attempt {attempt}/{REMOTE_ATTEMPTS}): {exc}", flush=True); time.sleep(REMOTE_DELAY_SECONDS)
    print(f"Public-site contract OK: {args.site_dir if args.site_dir else args.base_url}")


if __name__ == "__main__": main()
