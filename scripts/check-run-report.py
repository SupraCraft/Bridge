#!/usr/bin/env python3
"""Validate a Bridge structured run report without third-party dependencies."""

from __future__ import annotations

import json
import os
import pathlib
import re
import sys

DIAGNOSTIC_ID = re.compile(r"^BRIDGE-[WE][0-9]{3}$")
STATUSES = {"success", "no-op", "skipped", "failed"}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(f"Bridge run report invalid: {message}")


def nonnegative_int(value: object, name: str) -> None:
    require(isinstance(value, int) and not isinstance(value, bool) and value >= 0, f"{name} must be a non-negative integer")


def main() -> int:
    if len(sys.argv) != 2:
        raise SystemExit("usage: check-run-report.py <bridge-report.json>")

    path = pathlib.Path(sys.argv[1])
    raw = path.read_text(encoding="utf-8")
    report = json.loads(raw)

    expected_top = {
        "schema", "status", "bridgeVersion", "asmVersion", "hostJavaVersion",
        "classes", "transformations", "timingNanos", "diagnostics",
    }
    require(set(report) == expected_top, f"unexpected top-level keys: {sorted(set(report) ^ expected_top)}")
    require(report["schema"] == "bridge-run/1", "schema must be bridge-run/1")
    require(report["status"] in STATUSES, f"unknown status {report['status']!r}")
    for key in ("bridgeVersion", "asmVersion", "hostJavaVersion"):
        require(isinstance(report[key], str) and report[key], f"{key} must be a non-empty string")

    classes = report["classes"]
    require(set(classes) == {"examined", "transformed"}, "classes shape mismatch")
    nonnegative_int(classes["examined"], "classes.examined")
    nonnegative_int(classes["transformed"], "classes.transformed")
    require(classes["transformed"] <= classes["examined"], "transformed classes cannot exceed examined classes")

    transformations = report["transformations"]
    expected_transformations = {"bridges", "invocations", "adjustments", "removals", "forks"}
    require(set(transformations) == expected_transformations, "transformations shape mismatch")
    for key in sorted(expected_transformations):
        nonnegative_int(transformations[key], f"transformations.{key}")

    timing = report["timingNanos"]
    require(set(timing) == {"hierarchyScan", "transform", "total"}, "timingNanos shape mismatch")
    for key in ("hierarchyScan", "transform", "total"):
        nonnegative_int(timing[key], f"timingNanos.{key}")

    diagnostics = report["diagnostics"]
    require(isinstance(diagnostics, list), "diagnostics must be an array")
    for index, diagnostic in enumerate(diagnostics):
        require(isinstance(diagnostic, dict), f"diagnostics[{index}] must be an object")
        require(set(diagnostic).issubset({"id", "severity", "message", "causeType"}), f"diagnostics[{index}] has unknown fields")
        require({"id", "severity", "message"}.issubset(diagnostic), f"diagnostics[{index}] is missing required fields")
        require(isinstance(diagnostic["id"], str) and DIAGNOSTIC_ID.fullmatch(diagnostic["id"]), f"diagnostics[{index}].id is invalid")
        require(diagnostic["severity"] in {"warning", "error"}, f"diagnostics[{index}].severity is invalid")
        require(isinstance(diagnostic["message"], str) and diagnostic["message"], f"diagnostics[{index}].message must be non-empty")
        if "causeType" in diagnostic:
            require(isinstance(diagnostic["causeType"], str) and diagnostic["causeType"], f"diagnostics[{index}].causeType must be non-empty")

    # Reports are operational evidence and intentionally avoid leaking host-specific paths.
    for sensitive in (os.getcwd(), os.path.expanduser("~")):
        if sensitive and sensitive != "/":
            require(sensitive not in raw, "report contains an absolute environment path")

    print(f"Bridge run report OK: {path} ({report['status']}, {classes['transformed']}/{classes['examined']} classes transformed)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
