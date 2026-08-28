# Artifact identity and versioning

Bridge is a SupraCraft-maintained fork of upstream work. Published artifacts from this repository must identify the fork as the producer while preserving upstream attribution and keeping changes straightforward to contribute upstream.

## Identity policy

Published Maven coordinates are owned by this repository, not by the upstream project.

Target coordinates:

- group: `io.github.supracraft.bridge`
- parent: `io.github.supracraft.bridge:bridge-parent`
- modules: `bridge`, `bridge-asm`, `bridge-plugin`, and other module artifact IDs remain lowercase and descriptive

The legacy `net.ME1312.ASM:*` coordinates are upstream-derived compatibility coordinates and are not the canonical identity for new SupraCraft builds.

Java implementation packages are a separate compatibility concern. Existing neutral `bridge.*` source packages are retained unless an actual collision or API requirement justifies a breaking package migration. Fork identity belongs in Maven coordinates, artifact metadata, SBOM metadata, repository/source metadata, and release filenames rather than in source churn that would make upstream flow-back harder.

## Artifact metadata

Every distributable artifact should record at least:

- `Implementation-Vendor: SupraCraft`
- source repository: `SupraCraft/Bridge`
- exact source commit
- exact project version
- upstream repository/base reference when known

CycloneDX metadata and release evidence should carry the same source identity.

Canonical standalone release filenames should be human-identifiable, for example:

- `supracraft-bridge-<version>.jar`
- `supracraft-bridge-asm-<version>.jar`

Maven repository filenames remain derived from Maven coordinates as required by normal repository layout.

## Version policy

Maven requires a version but does not require `SNAPSHOT`. The suffix `-SNAPSHOT` has special mutable repository semantics only when it is the actual suffix of the version.

Do not publish versions such as `0.1.0-SNAPSHOT.123`; Maven treats them as ordinary unique versions even though their names imply snapshot semantics.

Use these channels instead:

- source/development line: `0.1.0-dev`
- immutable main-branch CI build: `0.1.0-dev.<github-run-number>`
- release candidate: `0.1.0-rc.<n>`
- release: `0.1.0`

The exact Git SHA belongs in provenance metadata, not in the version string. Consumers must pin an exact Bridge version for reproducible/release builds. Integration CI may resolve the newest `-dev.<run>` build, but must record the exact version it consumed.

## Upstream relationship

Do not encode upstream ownership in the published group ID. Preserve upstream attribution in README/license/NOTICE/source metadata and record the upstream base tag or commit independently from the SupraCraft artifact version. This keeps fork identity and upstream lineage as separate machine-readable facts.
