#!/usr/bin/env python3
"""Validate Bridge release qualification, candidate publication, and stable promotion contracts."""

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
    stable_schema_path = Path("schemas/stable-release.schema.json")
    candidate_validator_path = Path("scripts/validate-release-candidate.py")
    stable_validator_path = Path("scripts/validate-stable-release.py")
    context_resolver_path = Path("scripts/resolve-release-context.py")
    absent_path = Path("scripts/assert-maven-version-absent.sh")
    present_path = Path("scripts/assert-maven-version-present.sh")
    release_docs_path = Path("docs/release-candidates.md")

    paths = (
        workflow_path,
        aggregator_path,
        classfile_runner_path,
        modern_runner_path,
        candidate_schema_path,
        stable_schema_path,
        candidate_validator_path,
        stable_validator_path,
        context_resolver_path,
        absent_path,
        present_path,
        release_docs_path,
    )
    for path in paths:
        require(path.is_file(), f"missing {path}")

    workflow = workflow_path.read_text(encoding="utf-8")
    aggregator = aggregator_path.read_text(encoding="utf-8")
    classfile_runner = classfile_runner_path.read_text(encoding="utf-8")
    modern_runner = modern_runner_path.read_text(encoding="utf-8")
    candidate_validator = candidate_validator_path.read_text(encoding="utf-8")
    stable_validator = stable_validator_path.read_text(encoding="utf-8")
    context_resolver = context_resolver_path.read_text(encoding="utf-8")
    absent = absent_path.read_text(encoding="utf-8")
    present = present_path.read_text(encoding="utf-8")
    candidate_schema = json.loads(candidate_schema_path.read_text(encoding="utf-8"))
    stable_schema = json.loads(stable_schema_path.read_text(encoding="utf-8"))

    # Machine contract alignment.
    require(contract["compatibility"]["release_qualification_workflow"] == str(workflow_path),
            "PROJECT_CONTRACT compatibility workflow mismatch")
    require(contract["validation"]["release_qualification_workflow"] == str(workflow_path),
            "PROJECT_CONTRACT validation workflow mismatch")
    require(contract["validation"]["release_qualification_aggregator"] == str(aggregator_path),
            "PROJECT_CONTRACT aggregator mismatch")
    require(contract["validation"]["release_context_resolver"] == str(context_resolver_path),
            "PROJECT_CONTRACT release context resolver mismatch")
    require(contract["validation"]["release_candidate_validator"] == str(candidate_validator_path),
            "PROJECT_CONTRACT candidate validator mismatch")
    require(contract["validation"]["stable_release_validator"] == str(stable_validator_path),
            "PROJECT_CONTRACT stable validator mismatch")
    require(contract["validation"]["release_coordinate_absence_check"] == str(absent_path),
            "PROJECT_CONTRACT release absence check mismatch")
    require(contract["validation"]["release_coordinate_presence_check"] == str(present_path),
            "PROJECT_CONTRACT release presence check mismatch")
    require(contract["validation"]["publication_model"] == "build-once-promote-tested-bytes",
            "publication model mismatch")

    provenance = contract["provenance"]
    for key in (
        "release_qualification_binds_source_sha",
        "release_qualification_binds_bridge_version",
        "release_candidate_coordinates_immutable",
        "stable_release_coordinates_immutable",
        "stable_release_requires_qualified_candidate",
        "stable_tag_source_version_matches_coordinate",
    ):
        require(provenance[key] is True, f"provenance flag must remain true: {key}")
    require(provenance["release_candidate_publish_rebuilds"] is False,
            "candidate publication must not rebuild")
    require(provenance["stable_release_publish_rebuilds"] is False,
            "stable publication must not rebuild")

    versioning = contract["versioning"]
    require(versioning["release_candidate_branch"] == "release/X.Y.Z-rc.N",
            "candidate branch contract mismatch")
    require(versioning["release_candidate_manifest"] == "RELEASE_CANDIDATE.json",
            "candidate manifest contract mismatch")
    require(versioning["stable_release_branch"] == "stable/X.Y.Z",
            "stable branch contract mismatch")
    require(versioning["stable_release_manifest"] == "STABLE_RELEASE.json",
            "stable manifest contract mismatch")
    require(versioning["release_tag"] == "vX.Y.Z", "stable tag contract mismatch")
    require(contract["retention"]["release_qualification_evidence_days"] == 90,
            "release qualification evidence retention mismatch")
    require(contract["retention"]["release_candidate_evidence_days"] == 90,
            "candidate evidence retention mismatch")
    require(contract["retention"]["stable_release_evidence_days"] == 90,
            "stable evidence retention mismatch")

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
    require(policy["exact_bridge_version_required"] is True, "exact release version must be required")
    require(policy["reproducibility_evidence_consistency_required"] is True,
            "release evidence consistency must be required")
    require(policy["release_candidate_trigger"] == "release/X.Y.Z-rc.N + RELEASE_CANDIDATE.json",
            "candidate trigger mismatch")
    require(policy["stable_release_trigger"] == "stable/X.Y.Z + STABLE_RELEASE.json",
            "stable trigger mismatch")
    require(policy["stable_release_requires_published_qualified_candidate"] is True,
            "stable release must require a qualified candidate")
    require(policy["stable_release_source_version_matches_coordinate"] is True,
            "stable release source must carry its coordinate")
    require(policy["consumer_qualification_required_separately"] is True,
            "consumer qualification must remain a stable gate")
    require(policy["consumer"] == compatibility["consumer_qualification"]["reference_consumer"],
            "consumer reference mismatch")

    # Candidate and stable manifest contracts.
    require(candidate_schema["properties"]["schema"]["const"] == "bridge-release-candidate/1",
            "candidate schema identifier mismatch")
    require(candidate_schema["properties"]["immutable"]["const"] is True,
            "candidate schema must require immutability")
    require(stable_schema["properties"]["schema"]["const"] == "bridge-stable-release/1",
            "stable schema identifier mismatch")
    require(stable_schema["properties"]["immutable"]["const"] is True,
            "stable schema must require immutability")
    require(stable_schema["properties"]["publication"]["const"] == "github-packages",
            "stable schema publication mismatch")
    require("refs/heads/release/{version}" in candidate_validator,
            "candidate validator must bind exact release branch")
    require("refs/heads/stable/{version}" in stable_validator,
            "stable validator must bind exact stable branch")
    require('status.get("eligible_for_stable_promotion") is not True' in stable_validator,
            "stable validator must require promotion eligibility")
    require('consumer_qualification", {}).get("status") != "pass"' in stable_validator,
            "stable validator must require consumer qualification")
    require('source_version != version' in stable_validator,
            "stable validator must require checked-in source version to match stable coordinate")
    require('args.ref.startswith("refs/heads/release/")' in context_resolver,
            "context resolver missing candidate mode")
    require('args.ref.startswith("refs/heads/stable/")' in context_resolver,
            "context resolver missing stable mode")

    # Shared release matrix and exact-version behavior.
    required_workflow_fragments = (
        "branches: [main, master, 'release/**', 'stable/**']",
        "python3 scripts/resolve-release-context.py",
        "needs.context.outputs.is_release == 'true'",
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
        "bridge-release-build-${{ needs.context.outputs.version }}",
        "bridge-release-publication-${{ needs.context.outputs.version }}",
        "bash scripts/deploy-tested-artifacts.sh",
        "retention-days: 90",
    )
    for fragment in required_workflow_fragments:
        require(fragment in workflow, f"workflow missing required fragment: {fragment}")

    for host in policy["blocking_host_jvms"]:
        require(f"'{host}'" in workflow, f"workflow missing host JDK {host}")
    for host in policy["classfile_boundary_hosts"]:
        require(f"'{host}'" in workflow, f"workflow missing class-file boundary host {host}")
    for host in policy["modern_bytecode_boundary_hosts"]:
        require(f"'{host}'" in workflow, f"workflow missing modern-bytecode boundary host {host}")

    require("bridge-release-qualification/1" in aggregator, "aggregator schema mismatch")
    require('parser.add_argument("--bridge-version")' in aggregator,
            "aggregator must accept exact Bridge version")
    require('report.get("bridgeSourceVersion") == bridge_version' in aggregator,
            "aggregator must validate host evidence version")
    require('report.get("bridgeVersion") == bridge_version' in aggregator,
            "aggregator must validate boundary evidence version")

    for name, runner in (("class-file", classfile_runner), ("modern-bytecode", modern_runner)):
        require("BRIDGE_VERSION" in runner, f"{name} runner must accept exact Bridge version")
        require('help:evaluate -Dexpression=project.version' in runner,
                f"{name} runner must default to project version")
        require('"bridgeVersion": bridge_version' in runner,
                f"{name} evidence must record exact Bridge version")

    expected_modules = ("bridge-parent", "bridge", "bridge-asm", "bridge-plugin", "bridge-test")
    for module in expected_modules:
        require(module in absent, f"absence guard missing Maven module {module}")
        require(module in present, f"presence guard missing Maven module {module}")

    require("SOURCE_SHA: ${{ github.event.pull_request.head.sha || github.sha }}" in workflow,
            "workflow must bind exact PR head or pushed SHA")
    require(workflow.count("ref: ${{ env.SOURCE_SHA }}") == 8,
            "every source-consuming release job must check out exact SOURCE_SHA")
    require("needs: [context, release-build, host-jvms, classfile-boundary, modern-boundary]" in workflow,
            "verdict prerequisites mismatch")
    require(workflow.count("packages: write") == 2,
            "only candidate and stable publication jobs may write packages")
    require(workflow.count("contents: write") == 1,
            "only stable publication may write repository contents")

    candidate_marker = "  candidate-publish:\n"
    stable_marker = "  stable-publish:\n"
    require(candidate_marker in workflow, "candidate-publish job missing")
    require(stable_marker in workflow, "stable-publish job missing")
    before_stable, stable_publish = workflow.split(stable_marker, 1)
    candidate_publish = before_stable.split(candidate_marker, 1)[1]

    require("needs.context.outputs.is_candidate == 'true'" in candidate_publish,
            "candidate publication must require candidate mode")
    require("needs.release-build.result == 'success'" in candidate_publish,
            "candidate publication must require tested release build")
    require("needs.verdict.result == 'success'" in candidate_publish,
            "candidate publication must require qualification verdict")
    require("packages: write" in candidate_publish and "contents: write" not in candidate_publish,
            "candidate publication permissions are too broad or incomplete")
    require("./mvnw -B verify" not in candidate_publish and "versions-maven-plugin" not in candidate_publish,
            "candidate write job must not rebuild or re-version")
    require(candidate_publish.count("assert-maven-version-absent.sh") == 1,
            "candidate publication must reconfirm coordinate absence")
    require(candidate_publish.count("assert-maven-version-present.sh") == 1,
            "candidate publication must verify coordinate visibility")

    require("needs.context.outputs.is_stable == 'true'" in stable_publish,
            "stable publication must require stable mode")
    require("needs.release-build.result == 'success'" in stable_publish,
            "stable publication must require tested release build")
    require("needs.verdict.result == 'success'" in stable_publish,
            "stable publication must require qualification verdict")
    require("contents: write" in stable_publish and "packages: write" in stable_publish,
            "stable publication needs tag/release and package permissions")
    require("eligible_for_stable_promotion" in stable_publish,
            "stable publication must recheck qualified-candidate eligibility")
    require("consumer_qualification" in stable_publish,
            "stable publication must recheck consumer qualification")
    require("git tag -a" in stable_publish and "git push origin \"refs/tags/${tag}\"" in stable_publish,
            "stable publication must create the immutable stable tag")
    require("softprops/action-gh-release@7c4723f7a335432393329f8f1c564994ce50185d" in stable_publish,
            "stable GitHub release action must remain commit-pinned")
    require("./mvnw -B verify" not in stable_publish and "versions-maven-plugin" not in stable_publish,
            "stable write job must not rebuild or re-version")
    require(stable_publish.count("assert-maven-version-absent.sh") == 1,
            "stable publication must reconfirm coordinate absence")
    require(stable_publish.count("assert-maven-version-present.sh") == 1,
            "stable publication must verify coordinate visibility")

    print("Release qualification, candidate publication, and stable promotion contract OK")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
