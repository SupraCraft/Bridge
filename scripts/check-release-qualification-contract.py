#!/usr/bin/env python3
"""Validate Bridge release-qualification and immutable candidate publication contracts."""

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
    classfile_runner_path = Path("scripts/run-classfile-compatibility.sh")
    modern_runner_path = Path("scripts/run-modern-bytecode-compatibility.sh")
    candidate_schema_path = Path("schemas/release-candidate.schema.json")
    candidate_validator_path = Path("scripts/validate-release-candidate.py")
    absent_path = Path("scripts/assert-maven-version-absent.sh")
    present_path = Path("scripts/assert-maven-version-present.sh")
    candidate_docs_path = Path("docs/release-candidates.md")
    for path in (
        workflow_path,
        aggregator_path,
        classfile_runner_path,
        modern_runner_path,
        candidate_schema_path,
        candidate_validator_path,
        absent_path,
        present_path,
        candidate_docs_path,
    ):
        require(path.is_file(), f"missing {path}")

    workflow = workflow_path.read_text(encoding="utf-8")
    aggregator = aggregator_path.read_text(encoding="utf-8")
    classfile_runner = classfile_runner_path.read_text(encoding="utf-8")
    modern_runner = modern_runner_path.read_text(encoding="utf-8")
    candidate_validator = candidate_validator_path.read_text(encoding="utf-8")
    absent = absent_path.read_text(encoding="utf-8")
    present = present_path.read_text(encoding="utf-8")
    candidate_schema = json.loads(candidate_schema_path.read_text(encoding="utf-8"))

    require(contract["compatibility"]["release_qualification_workflow"] == str(workflow_path),
            "PROJECT_CONTRACT compatibility workflow mismatch")
    require(contract["validation"]["release_qualification_workflow"] == str(workflow_path),
            "PROJECT_CONTRACT validation workflow mismatch")
    require(contract["validation"]["release_qualification_aggregator"] == str(aggregator_path),
            "PROJECT_CONTRACT aggregator mismatch")
    require(contract["validation"]["release_candidate_validator"] == str(candidate_validator_path),
            "PROJECT_CONTRACT candidate validator mismatch")
    require(contract["validation"]["release_candidate_coordinate_absence_check"] == str(absent_path),
            "PROJECT_CONTRACT candidate absence check mismatch")
    require(contract["validation"]["release_candidate_coordinate_presence_check"] == str(present_path),
            "PROJECT_CONTRACT candidate presence check mismatch")
    require(contract["provenance"]["release_qualification_binds_source_sha"] is True,
            "release qualification must bind source SHA")
    require(contract["provenance"]["release_qualification_binds_bridge_version"] is True,
            "release qualification must bind Bridge version")
    require(contract["provenance"]["release_candidate_coordinates_immutable"] is True,
            "release candidate coordinates must be immutable")
    require(contract["provenance"]["release_candidate_publish_rebuilds"] is False,
            "release candidate publication must not rebuild")
    require(contract["retention"]["release_qualification_evidence_days"] == 90,
            "aggregate evidence retention must remain 90 days")
    require(contract["retention"]["release_candidate_evidence_days"] == 90,
            "candidate evidence retention must remain 90 days")
    require(contract["versioning"]["release_candidate_branch"] == "release/X.Y.Z-rc.N",
            "candidate branch contract mismatch")
    require(contract["versioning"]["release_candidate_manifest"] == "RELEASE_CANDIDATE.json",
            "candidate manifest contract mismatch")
    require(contract["documentation"]["release_candidates"] == str(candidate_docs_path),
            "candidate documentation contract mismatch")

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
    require(policy["exact_bridge_version_required"] is True,
            "release qualification must require exact Bridge version")
    require(policy["release_candidate_trigger"] == "release/X.Y.Z-rc.N + RELEASE_CANDIDATE.json",
            "release candidate trigger mismatch")
    require(policy["release_candidate_publication"] == "github-packages-after-qualified-tested-build",
            "release candidate publication policy mismatch")
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
        "branches: [main, master, 'release/**']",
        "RELEASE_CANDIDATE.json",
        "python3 scripts/validate-release-candidate.py",
        "bash scripts/assert-maven-version-absent.sh",
        "bash scripts/assert-maven-version-present.sh",
        "org.codehaus.mojo:versions-maven-plugin:2.21.0:set",
        "./mvnw -B verify",
        "python3 scripts/check-run-report.py",
        "java scripts/VerifyClassFiles.java",
        "java -Xverify:all",
        "bash scripts/run-classfile-compatibility.sh",
        "bash scripts/run-modern-bytecode-compatibility.sh",
        "python3 scripts/aggregate-release-qualification.py",
        "--source-sha \"$SOURCE_SHA\"",
        "--bridge-version '${{ needs.context.outputs.version }}'",
        "build/release-qualification/bridge-release-qualification.json",
        "bridge-candidate-publication-${{ needs.context.outputs.version }}",
        "bash scripts/deploy-tested-artifacts.sh",
        "retention-days: 90",
    )
    for fragment in required_workflow_fragments:
        require(fragment in workflow, f"workflow missing required fragment: {fragment}")

    require("bridge-release-qualification/1" in aggregator, "aggregator schema mismatch")
    require('parser.add_argument("--bridge-version")' in aggregator,
            "aggregator must accept an exact Bridge version override")
    require('report.get("bridgeSourceVersion") == bridge_version' in aggregator,
            "aggregator must validate host evidence Bridge version")
    require('report.get("bridgeVersion") == bridge_version' in aggregator,
            "aggregator must validate boundary evidence Bridge version")
    require('"bridgeSourceVersion": bridge_version' in aggregator,
            "aggregate verdict must record the exact qualified Bridge version")
    require("consumerQualification" in aggregator, "aggregator must preserve consumer gate state")
    require("required-separately-before-stable" in aggregator,
            "aggregator must not imply stable consumer qualification has passed")

    for name, runner in (("class-file", classfile_runner), ("modern-bytecode", modern_runner)):
        require('BRIDGE_VERSION' in runner, f"{name} runner must accept an exact Bridge version")
        require('help:evaluate -Dexpression=project.version' in runner,
                f"{name} runner must default to the current project version")
        require('bridge-plugin:0.1.0-dev:bridge' not in runner,
                f"{name} runner must not hardcode the development plugin coordinate")
        require('"bridgeVersion": bridge_version' in runner,
                f"{name} evidence must record the exact Bridge version")

    require(candidate_schema["properties"]["schema"]["const"] == "bridge-release-candidate/1",
            "candidate schema identifier mismatch")
    require(candidate_schema["properties"]["immutable"]["const"] is True,
            "candidate schema must require immutability")
    require(candidate_schema["properties"]["publication"]["const"] == "github-packages",
            "candidate schema publication mismatch")
    require("refs/heads/release/{version}" in candidate_validator,
            "candidate validator must bind branch name to exact candidate version")
    require('source_version != f"{release_line}-dev"' in candidate_validator,
            "candidate validator must require development source POM")

    expected_modules = ("bridge-parent", "bridge", "bridge-asm", "bridge-plugin", "bridge-test")
    for module in expected_modules:
        require(module in absent, f"absence guard missing Maven module {module}")
        require(module in present, f"presence guard missing Maven module {module}")

    require("SOURCE_SHA: ${{ github.event.pull_request.head.sha || github.sha }}" in workflow,
            "workflow must bind evidence to PR head SHA or pushed SHA")
    require(workflow.count("ref: ${{ env.SOURCE_SHA }}") == 7,
            "every source-consuming release job must check out the exact SOURCE_SHA")
    require("needs: [context, candidate-build, host-jvms, classfile-boundary, modern-boundary]" in workflow,
            "verdict prerequisites mismatch")
    require("needs: [context, candidate-build, verdict]" in workflow,
            "candidate publication prerequisites mismatch")
    require(workflow.count("packages: write") == 1,
            "only one narrowly scoped job may write GitHub Packages")

    publish_marker = "  candidate-publish:\n"
    require(publish_marker in workflow, "candidate-publish job is missing")
    publish = workflow.split(publish_marker, 1)[1]
    require("needs.context.outputs.is_candidate == 'true'" in publish,
            "candidate publication must require candidate mode")
    require("needs.candidate-build.result == 'success'" in publish,
            "candidate publication must require tested candidate build")
    require("needs.verdict.result == 'success'" in publish,
            "candidate publication must require aggregate qualification verdict")
    require("packages: write" in publish, "candidate publication job must own package write permission")
    require("bash scripts/deploy-tested-artifacts.sh" in publish,
            "candidate publication must promote tested bytes")
    require("bridge-candidate-publication-${{ needs.context.outputs.version }}" in publish,
            "candidate publication must download the tested Maven handoff")
    require("./mvnw -B verify" not in publish and "mvnw -B clean" not in publish and "versions-maven-plugin" not in publish,
            "write-capable candidate publication job must not rebuild or re-version")
    require(publish.count("assert-maven-version-absent.sh") == 1,
            "candidate publication must reconfirm coordinate absence immediately before deploy")
    require(publish.count("assert-maven-version-present.sh") == 1,
            "candidate publication must verify coordinate visibility after deploy")

    print("Release qualification and candidate publication contract OK")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
