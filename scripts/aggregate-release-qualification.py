#!/usr/bin/env python3
"""Aggregate Bridge release-qualification evidence into one deterministic verdict."""

from __future__ import annotations

import argparse
import json
from pathlib import Path

HOSTS = (21, 22, 23, 24, 25, 26)
BOUNDARY_HOSTS = (21, 26)
CLASSFILE_TARGETS = (8, 11, 17, 21, 22, 23, 24, 25, 26)
MODERN_CASES = (
    "java21-modern-language-bytecode",
    "java25-flexible-constructor",
    "java21-module-info",
)


def load(path: Path) -> dict:
    if not path.is_file():
        raise SystemExit(f"Missing qualification evidence: {path}")
    return json.loads(path.read_text(encoding="utf-8"))


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(f"Release qualification failed: {message}")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--evidence-root", type=Path, required=True)
    parser.add_argument("--source-sha", required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()

    host_summaries = []
    for host in HOSTS:
        report = load(args.evidence_root / "hosts" / f"host-jdk-{host}.json")
        require(report.get("schema") == "bridge-host-qualification/1", f"host {host}: wrong schema")
        require(report.get("sourceCommit") == args.source_sha, f"host {host}: source SHA mismatch")
        require(report.get("hostJavaFeature") == host, f"host {host}: wrong host feature")
        checks = report.get("checks", {})
        for name in ("reactorVerify", "runReport", "bytecodeBaseline", "multiReleaseRuntime"):
            require(checks.get(name) == "pass", f"host {host}: {name} did not pass")
        expected_classfile_api = "pass" if host >= 24 else "not-applicable"
        require(checks.get("jdkClassFileApiVerify") == expected_classfile_api,
                f"host {host}: JDK Class-File API state mismatch")
        host_summaries.append(report)

    classfile_summaries = []
    for host in BOUNDARY_HOSTS:
        report = load(args.evidence_root / "classfiles" / f"host-{host}.json")
        require(report.get("schema") == "bridge-classfile-matrix/1", f"classfile host {host}: wrong schema")
        require(report.get("hostJavaFeature") == host, f"classfile host {host}: wrong host")
        targets = report.get("targets", [])
        require(tuple(item.get("javaRelease") for item in targets) == CLASSFILE_TARGETS,
                f"classfile host {host}: target set mismatch")
        for item in targets:
            for name in ("bridgeTransform", "bridgeRunReport", "classFileApiVerify", "jvmVerifyAll", "execution"):
                require(item.get(name) == "pass", f"classfile host {host} Java {item.get('javaRelease')}: {name} did not pass")
        classfile_summaries.append(report)

    modern_summaries = []
    for host in BOUNDARY_HOSTS:
        report = load(args.evidence_root / "modern" / f"host-{host}.json")
        require(report.get("schema") == "bridge-modern-bytecode/1", f"modern host {host}: wrong schema")
        require(report.get("hostJavaFeature") == host, f"modern host {host}: wrong host")
        cases = report.get("cases", [])
        require(tuple(item.get("id") for item in cases) == MODERN_CASES,
                f"modern host {host}: case set mismatch")
        for item in cases:
            for name in ("bridgeTransform", "classFileApiVerify", "jvmVerifyAll", "execution"):
                require(item.get(name) == "pass", f"modern host {host} {item.get('id')}: {name} did not pass")
        modern_summaries.append(report)

    verdict = {
        "schema": "bridge-release-qualification/1",
        "status": "pass",
        "sourceCommit": args.source_sha,
        "bridgeSourceVersion": "0.1.0-dev",
        "hostJvm": {
            "qualified": list(HOSTS),
            "minimum": 21,
        },
        "inputClassFiles": {
            "qualified": list(CLASSFILE_TARGETS),
            "crossHostTransform": list(BOUNDARY_HOSTS),
        },
        "modernBytecode": {
            "qualifiedCases": list(MODERN_CASES),
            "crossHostTransform": list(BOUNDARY_HOSTS),
        },
        "verification": {
            "reactor": "pass",
            "structuredRunReport": "pass",
            "asm": "pass",
            "jdkClassFileApi": "pass-on-applicable-jdks",
            "jvmVerifyAll": "pass",
            "behavioralExecution": "pass",
            "multiReleaseRuntime": "pass",
        },
        "advisoryNextJdk": 27,
        "consumerQualification": "required-separately-before-stable",
    }

    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(verdict, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(verdict, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
