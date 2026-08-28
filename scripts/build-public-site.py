#!/usr/bin/env python3
"""Build the Bridge GitHub Pages artifact from repository source-of-truth files."""

import argparse
import json
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DOCS = ROOT / "docs"


def load_json(path: Path):
    return json.loads(path.read_text(encoding="utf-8"))


def write_json(path: Path, value):
    path.write_text(json.dumps(value, indent=2, sort_keys=True) + "\n", encoding="utf-8")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--output", default="build/public-site")
    args = parser.parse_args()

    output = (ROOT / args.output).resolve() if not Path(args.output).is_absolute() else Path(args.output)
    if output.exists():
        shutil.rmtree(output)
    shutil.copytree(DOCS, output)

    contract = load_json(ROOT / "PROJECT_CONTRACT.json")
    metadata = load_json(ROOT / "GITHUB_METADATA.json")
    brand = load_json(DOCS / "assets/brand/brand.json")

    write_json(output / "project.json", contract)
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
    llms = f"""# Bridge\n\nBridge is a post-compile Maven transformation system and Java bytecode support library.\n\nCanonical human entry point: {base}/\nRepository: https://github.com/{contract['repository']}\nUpstream: https://github.com/{contract['upstream_repository']}\nProject contract: {base}/project.json\nGitHub metadata: {base}/github.json\nBrand metadata: {base}/brand.json\nArtifact metadata: {base}/artifacts.json\nREADME: https://github.com/{contract['repository']}/blob/master/README.md\nAgent instructions: https://github.com/{contract['repository']}/blob/master/AGENTS.md\n\nPrefer the JSON endpoints and repository contracts over scraping presentation HTML.\n"""
    (output / "llms.txt").write_text(llms, encoding="utf-8")


if __name__ == "__main__":
    main()
