#!/usr/bin/env python3
"""Validate a Bridge stable-release manifest and its qualified-candidate binding."""

from __future__ import annotations

import argparse
import json
import re
import xml.etree.ElementTree as ET
from pathlib import Path

STABLE_PATTERN = re.compile(r"^(?P<line>\d+\.\d+\.\d+)$")
RC_PATTERN = re.compile(r"^(?P<line>\d+\.\d+\.\d+)-rc\.(?P<number>[1-9]\d*)$")


def fail(message: str) -> None:
    raise SystemExit(f"Stable release invalid: {message}")


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
    parser.add_argument("--ref", help="Expected full Git ref, e.g. refs/heads/stable/0.1.0")
    parser.add_argument("--github-output", type=Path)
    args = parser.parse_args()

    if not args.manifest.is_file():
        fail(f"manifest does not exist: {args.manifest}")
    data = json.loads(args.manifest.read_text(encoding="utf-8"))
    expected_keys = {
        "schema", "version", "tag", "candidate", "candidate_status",
        "target_branch", "publication", "immutable"
    }
    if set(data) != expected_keys:
        fail(f"manifest keys must be exactly {sorted(expected_keys)}")
    if data["schema"] != "bridge-stable-release/1":
        fail("unexpected schema")
    if data["target_branch"] != "master":
        fail("target_branch must be master")
    if data["publication"] != "github-packages":
        fail("publication must be github-packages")
    if data["immutable"] is not True:
        fail("immutable must be true")

    version = data["version"]
    if not isinstance(version, str) or not STABLE_PATTERN.fullmatch(version):
        fail("version must match X.Y.Z")
    if data["tag"] != f"v{version}":
        fail("tag must be v<version>")

    candidate = data["candidate"]
    if not isinstance(candidate, str):
        fail("candidate must be a string")
    candidate_match = RC_PATTERN.fullmatch(candidate)
    if not candidate_match or candidate_match.group("line") != version:
        fail("candidate must be an X.Y.Z-rc.N from the same release line")

    expected_status = f"release-candidates/{candidate}.json"
    if data["candidate_status"] != expected_status:
        fail(f"candidate_status must be {expected_status}")
    status_path = Path(expected_status)
    if not status_path.is_file():
        fail(f"candidate status does not exist: {status_path}")
    status = json.loads(status_path.read_text(encoding="utf-8"))
    if status.get("schema") != "bridge-release-candidate-status/1":
        fail("candidate status schema mismatch")
    if status.get("version") != candidate:
        fail("candidate status version mismatch")
    if status.get("status") != "published-qualified":
        fail("candidate must be published-qualified")
    if status.get("eligible_for_stable_promotion") is not True:
        fail("candidate must be eligible for stable promotion")
    if status.get("consumer_qualification", {}).get("status") != "pass":
        fail("candidate consumer qualification must pass")
    promotion = status.get("stable_promotion", {})
    if promotion.get("decision") != "eligible" or promotion.get("remaining_blockers") != []:
        fail("candidate stable-promotion verdict must be eligible with no blockers")

    source_version = project_version()
    if source_version != version:
        fail(f"stable source POM must be {version}; found {source_version}")

    expected_ref = f"refs/heads/stable/{version}"
    if args.ref and args.ref != expected_ref:
        fail(f"stable {version} must run from {expected_ref}; got {args.ref}")

    if args.github_output:
        args.github_output.parent.mkdir(parents=True, exist_ok=True)
        with args.github_output.open("a", encoding="utf-8") as handle:
            handle.write(f"version={version}\n")
            handle.write(f"release_line={version}\n")
            handle.write(f"candidate={candidate}\n")
            handle.write(f"candidate_source={status['source_commit']}\n")
            handle.write(f"tag={data['tag']}\n")

    print(f"Bridge stable release manifest OK: {version} from {candidate}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
