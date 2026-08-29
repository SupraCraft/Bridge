#!/usr/bin/env python3
"""Validate that Bridge release-qualification policy and workflow remain aligned."""

from __future__ import annotations

import json
from pathlib import Path


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(f"Release qualification contract invalid: {message}")


def main() -> int:
    contract = json.loads(Path("PROJECT_CONTRACT.json").read_text(encoding="utf-8"))
    compatibility = json.loads(Path("COMPATIBILITY.json").read_text(encoding="utf-8"))
    workflow_path = Path(".github/workflows/release-qualification.yml")
    aggregator_path = Path("scripts/aggregate-release-qualification.py")
    require(workflow_path.is_file(), f"missing {workflow_path}")
    require(aggregator_path.is_file(), f"missing {aggregator_path}")
    workflow = workflow_path.read_text(encoding="utf-8")
    aggregator = aggregator_path.read_text(encoding="utf-8")

    require(contract["compatibility"]["release_qualification_workflow"] == str(workflow_path),
            "PROJECT_CONTRACT compatibility workflow mismatch")
    require(contract["validation"]["release_qualification_workflow"] == str(workflow_path),
            "PROJECT_CONTRACT validation workflow mismatch")
    require(contract["validation"]["release_qualification_aggregator"] == str(aggregator_path),
            "PROJECT_CONTRACT aggregator mismatch")
    require(contract["provenance"]["release_qualification_binds_source_sha"] is True,
            "release qualification must bind source SHA")
    require(contract["retention"]["release_qualification_evidence_days"] == 90,
            "aggregate evidence retention must remain 90 days")

    policy = compatibility["release_qualification"]
    require(policy["workflow"] == str(workflow_path), "COMPATIBILITY workflow mismatch")
    require(policy["aggregator"] == str(aggregator_path), "COMPATIBILITY aggregator mismatch")
    require(policy["evidence_schema"] == "bridge-release-qualification/1", "unexpected evidence schema")
    require(policy["blocking_host_jvms"] == [21, 22, 23, 24, 25, 26], "blocking host set mismatch")
    require(policy["blocking_host_jvms"] == compatibility["host_jvm"]["release_qualification"],
            "release host sets disagree")
    require(policy["classfile_boundary_hosts"] == compatibility["class_files"]["cross_host_transform"],
            "class-file boundary hosts disagree")
    require(policy["modern_bytecode_boundary_hosts"] == compatibility["modern_bytecode"]["cross_host_transform"],
            "modern-bytecode boundary hosts disagree")
    require(policy["next_jdk_advisory"] == 27, "next-JDK advisory mismatch")
    require(policy["consumer_qualification_required_separately"] is True,
            "consumer qualification must remain a separate stable-release gate")
    require(policy["consumer"] == compatibility["consumer_qualification"]["reference_consumer"],
            "consumer reference mismatch")

    for host in policy["blocking_host_jvms"]:
        require(f"'{host}'" in workflow, f"workflow is missing release host JDK {host}")
    for host in policy["classfile_boundary_hosts"]:
        require(f"'{host}'" in workflow, f"workflow is missing class-file boundary host {host}")
    for host in policy["modern_bytecode_boundary_hosts"]:
        require(f"'{host}'" in workflow, f"workflow is missing modern-bytecode boundary host {host}")

    required_workflow_fragments = (
        "./mvnw -B verify",
        "python3 scripts/check-run-report.py",
        "java scripts/VerifyClassFiles.java",
        "java -Xverify:all",
        "bash scripts/run-classfile-compatibility.sh",
        "bash scripts/run-modern-bytecode-compatibility.sh",
        "python3 scripts/aggregate-release-qualification.py",
        "--source-sha \"$SOURCE_SHA\"",
        "build/release-qualification/bridge-release-qualification.json",
        "retention-days: 90",
    )
    for fragment in required_workflow_fragments:
        require(fragment in workflow, f"workflow missing required fragment: {fragment}")

    require("bridge-release-qualification/1" in aggregator, "aggregator schema mismatch")
    require("consumerQualification" in aggregator, "aggregator must preserve consumer gate state")
    require("required-separately-before-stable" in aggregator,
            "aggregator must not imply stable consumer qualification has passed")
    require("SOURCE_SHA: ${{ github.event.pull_request.head.sha || github.sha }}" in workflow,
            "workflow must bind evidence to PR head SHA or pushed SHA")
    require("if: always()" in workflow, "verdict must run and fail closed when prerequisite jobs fail")
    require("needs: [host-jvms, classfile-boundary, modern-boundary]" in workflow,
            "verdict prerequisites mismatch")

    print("Release qualification contract OK")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
