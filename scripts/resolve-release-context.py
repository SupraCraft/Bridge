#!/usr/bin/env python3
"""Resolve Bridge development, release-candidate, or stable qualification context."""

from __future__ import annotations

import argparse
import subprocess
import sys
import xml.etree.ElementTree as ET
from pathlib import Path


def project_version() -> str:
    root = ET.parse("pom.xml").getroot()
    ns = {"m": "http://maven.apache.org/POM/4.0.0"}
    node = root.find("m:version", ns)
    if node is None or not node.text or not node.text.strip():
        raise SystemExit("Unable to resolve project version from pom.xml")
    return node.text.strip()


def append_output(path: Path, values: dict[str, str]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("a", encoding="utf-8") as handle:
        for key, value in values.items():
            handle.write(f"{key}={value}\n")


def run_validator(script: str, manifest: str, ref: str, output: Path) -> dict[str, str]:
    scratch = output.with_name(output.name + ".release-context")
    scratch.unlink(missing_ok=True)
    subprocess.run(
        [sys.executable, script, manifest, "--ref", ref, "--github-output", str(scratch)],
        check=True,
    )
    values: dict[str, str] = {}
    for line in scratch.read_text(encoding="utf-8").splitlines():
        key, value = line.split("=", 1)
        values[key] = value
    scratch.unlink(missing_ok=True)
    return values


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--ref", required=True)
    parser.add_argument("--github-output", type=Path, required=True)
    args = parser.parse_args()

    if args.ref.startswith("refs/heads/release/"):
        values = run_validator(
            "scripts/validate-release-candidate.py",
            "RELEASE_CANDIDATE.json",
            args.ref,
            args.github_output,
        )
        values.update({"mode": "candidate", "is_candidate": "true", "is_stable": "false", "is_release": "true", "tag": ""})
    elif args.ref.startswith("refs/heads/stable/"):
        values = run_validator(
            "scripts/validate-stable-release.py",
            "STABLE_RELEASE.json",
            args.ref,
            args.github_output,
        )
        values.update({"mode": "stable", "is_candidate": "false", "is_stable": "true", "is_release": "true"})
    else:
        version = project_version()
        line = version[:-4] if version.endswith("-dev") else version
        values = {
            "version": version,
            "release_line": line,
            "mode": "development",
            "is_candidate": "false",
            "is_stable": "false",
            "is_release": "false",
            "candidate": "",
            "candidate_source": "",
            "tag": "",
        }

    append_output(args.github_output, values)
    print(f"Bridge qualification mode: {values['mode']} {values['version']}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
