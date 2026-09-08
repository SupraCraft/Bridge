# Artifact and package retention policy

Bridge has three different persistence classes. They must not be treated as interchangeable cleanup targets.

## GitHub Actions artifacts

Actions artifacts are transient validation and handoff material, not the permanent publication record.

- Ordinary validated build evidence is retained for 14 days.
- The exact tested Maven publication bundle is retained for 1 day because it exists only to hand tested bytes from the build job to the publication job.
- Expired Actions artifacts may disappear without changing the identity or validity of a published release or Maven package.

## GitHub Releases

Release tags and release assets are durable historical provenance. Do not delete or rewrite them merely because current naming, packaging, or implementation policy has changed.

A historical release may legitimately contain names or metadata that are no longer used by current builds.

## Maven package versions

Published Maven versions are dependencies, not disposable CI cache entries. An apparently old development version can still be required by a maintained source tree, tag, release, or downstream reproducibility record.

Never delete a Bridge Maven version solely because a newer version exists.

Before deleting any `*-dev.<run>` version, a deterministic cleanup process must:

1. inventory the exact candidate package/version identities;
2. search maintained Bridge refs and known maintained consumer refs for exact references to each candidate;
3. preserve every referenced version;
4. preserve stable and release-candidate versions unless an explicit separate retention decision says otherwise;
5. produce a dry-run report for review before destructive mutation; and
6. re-check references immediately before deletion so changed state fails closed.

## Stable-release gate for development-package pruning

Broad development-package pruning is deferred until Bridge has a stable release line and maintained consumers that require reproducible builds have moved from disposable development coordinates to suitable immutable stable coordinates.

This is a safety gate, not a requirement to delete old packages once the gate is satisfied. Package cleanup should be implemented only when package accumulation creates a concrete operational or storage burden.

## Source of truth

Executable retention periods live in GitHub Actions workflows. This document defines the durable policy boundaries that those workflows and any future package-cleanup automation must preserve.
