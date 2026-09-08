#!/usr/bin/env python3
"""Build the Bridge GitHub Pages artifact from repository source-of-truth files."""

import argparse
import json
import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DOCS = ROOT / "docs"


def load_json(path: Path):
    return json.loads(path.read_text(encoding="utf-8"))


def write_json(path: Path, value):
    path.write_text(json.dumps(value, indent=2, sort_keys=True) + "\n", encoding="utf-8")


def replace_fact(page: str, pattern: str, value: str, label: str) -> str:
    rendered, count = re.subn(
        pattern,
        lambda match: f"{match.group(1)}{value}{match.group(2)}",
        page,
        count=1,
    )
    if count != 1:
        raise RuntimeError(f"unable to render {label} from repository contracts")
    return rendered


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--output", default="build/public-site")
    args = parser.parse_args()

    output = (ROOT / args.output).resolve() if not Path(args.output).is_absolute() else Path(args.output)
    if output.exists():
        shutil.rmtree(output)
    shutil.copytree(DOCS, output)

    contract = load_json(ROOT / "PROJECT_CONTRACT.json")
    compatibility = load_json(ROOT / "COMPATIBILITY.json")
    metadata = load_json(ROOT / "GITHUB_METADATA.json")
    brand = load_json(DOCS / "assets/brand/brand.json")

    page_path = output / "index.html"
    page = page_path.read_text(encoding="utf-8")
    page = replace_fact(
        page,
        r'(<code id="source-version">)[^<]*(</code>)',
        contract["source_version"],
        "source version",
    )
    page = replace_fact(
        page,
        r'(<span id="java-release">)[^<]*(</span>)',
        str(contract["toolchain"]["java_bytecode_release"]),
        "Java bytecode release",
    )
    page = replace_fact(
        page,
        r'(<span id="asm-version">)[^<]*(</span>)',
        compatibility["asm"]["version"],
        "ASM version",
    )
    page = replace_fact(
        page,
        r'(<span id="host-jvms">)[^<]*(</span>)',
        ", ".join(str(value) for value in compatibility["host_jvm"]["pull_request_blocking"]),
        "host JVM lanes",
    )
    page_path.write_text(page, encoding="utf-8")

    write_json(output / "project.json", contract)
    write_json(output / "compatibility.json", compatibility)
    write_json(output / "github.json", metadata)
    write_json(output / "brand.json", brand)
    write_json(
        output / "artifacts.json",
        {
            "schema_version": "1.0.0",
            "repository": contract["repository"],
            "source_version": contract["source_version"],
            "artifact": contract["artifact"],
            "versioning": contract["versioning"],
            "provenance": contract["provenance"],
        },
    )

    base = metadata["homepage"].rstrip("/")
    llms = f"""# Bridge\n\nBridge is a post-compile Maven transformation system and Java bytecode support library.\n\nCanonical human entry point: {base}/\nRepository: https://github.com/{contract['repository']}\nUpstream: https://github.com/{contract['upstream_repository']}\nProject contract: {base}/project.json\nCompatibility policy: {base}/compatibility.json\nGitHub metadata: {base}/github.json\nBrand metadata: {base}/brand.json\nArtifact metadata: {base}/artifacts.json\nREADME: https://github.com/{contract['repository']}/blob/master/README.md\nAgent instructions: https://github.com/{contract['repository']}/blob/master/AGENTS.md\n\nPrefer the JSON endpoints and repository contracts over scraping presentation HTML. Compatibility policy defines intended qualification lanes; only completed CI/release evidence establishes tested support.\n"""
    (output / "llms.txt").write_text(llms, encoding="utf-8")


if __name__ == "__main__":
    main()
