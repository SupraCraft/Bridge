#!/usr/bin/env python3
"""Validate a Bridge release-candidate manifest and its release branch binding."""

from __future__ import annotations

import argparse
import json
import re
import xml.etree.ElementTree as ET
from pathlib import Path

RC_PATTERN = re.compile(r"^(?P<line>\d+\.\d+\.\d+)-rc\.(?P<number>[1-9]\d*)$")


def fail(message: str) -> None:
    raise SystemExit(f"Release candidate invalid: {message}")


def project_version() -> str:
    root = ET.parse("pom.xml").getroot()
    ns = {"m": "http://maven.apache.org/POM/4.0.0"}
    node = root.find("m:version", ns)
    if node is None or not node.text or not node.text.strip():
        fail("unable to resolve project version from pom.xml")
    return node.text.strip()


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("manifest", type=Path)
    parser.add_argument("--ref", help="Expected full Git ref, e.g. refs/heads/release/0.1.0-rc.1")
    parser.add_argument("--github-output", type=Path)
    args = parser.parse_args()

    if not args.manifest.is_file():
        fail(f"manifest does not exist: {args.manifest}")
    data = json.loads(args.manifest.read_text(encoding="utf-8"))
    expected_keys = {"schema", "version", "release_line", "target_branch", "publication", "immutable"}
    if set(data) != expected_keys:
        fail(f"manifest keys must be exactly {sorted(expected_keys)}")
    if data["schema"] != "bridge-release-candidate/1":
        fail("unexpected schema")
    if data["target_branch"] != "master":
        fail("target_branch must be master")
    if data["publication"] != "github-packages":
        fail("publication must be github-packages")
    if data["immutable"] is not True:
        fail("immutable must be true")

    version = data["version"]
    if not isinstance(version, str):
        fail("version must be a string")
    match = RC_PATTERN.fullmatch(version)
    if not match:
        fail("version must match X.Y.Z-rc.N with N >= 1")
    release_line = match.group("line")
    if data["release_line"] != release_line:
        fail("release_line must match the semantic-version prefix")

    source_version = project_version()
    if source_version != f"{release_line}-dev":
        fail(f"source POM must remain {release_line}-dev; found {source_version}")

    expected_ref = f"refs/heads/release/{version}"
    if args.ref and args.ref != expected_ref:
        fail(f"candidate {version} must run from {expected_ref}; got {args.ref}")

    if args.github_output:
        args.github_output.parent.mkdir(parents=True, exist_ok=True)
        with args.github_output.open("a", encoding="utf-8") as handle:
            handle.write(f"version={version}\n")
            handle.write(f"release_line={release_line}\n")
            handle.write(f"candidate_number={match.group('number')}\n")

    print(f"Bridge release candidate manifest OK: {version}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
