# Artifact identity and publication contract

Bridge is a SupraCraft-maintained fork of upstream work. Published artifacts from this repository identify SupraCraft as the producer while preserving upstream attribution and keeping source changes straightforward to contribute upstream.

## Canonical identity

Published Maven coordinates are owned by this repository:

- group: `io.github.supracraft.bridge`
- parent: `io.github.supracraft.bridge:bridge-parent`
- modules: `bridge`, `bridge-asm`, `bridge-plugin`, `bridge-test`

The optional `bridge-mc-it` module is an integration-test module and is not part of the normal published reactor artifact set.

Historical `net.ME1312.ASM:*` coordinates belong to upstream/history and must not be used as the identity of new SupraCraft artifacts.

Java implementation packages remain `bridge.*`. Fork identity belongs in Maven coordinates, artifact metadata, SBOM/build evidence, repository metadata, and release filenames rather than in source-package branding churn.

## Standalone artifacts

Actions/releases stage producer-qualified files separately from conventional Maven repository layout:

- `supracraft-bridge-<version>.jar`
- `supracraft-bridge-asm-<version>.jar`
- `supracraft-bridge-plugin-<version>.jar`
- `supracraft-bridge-sbom-<version>.json`
- `BUILD-METADATA.properties`
- `REPRODUCIBILITY.properties`
- `SHA256SUMS`

Maven repository filenames remain normal artifactId/version filenames. The standalone `supracraft-*` JARs are byte-identical copies of the corresponding tested Maven-built module JARs.

## Embedded provenance

Every distributable Bridge JAR records:

- `Implementation-Title`
- `Implementation-Version`
- `Implementation-Vendor: SupraCraft`
- `Build-Commit`
- `Build-Ref`
- `Build-Number`
- `Source-Repository: SupraCraft/Bridge`
- `Upstream-Repository: ME1312/Bridge`

CycloneDX/build evidence carries the same producer/upstream distinction.

## Build-once / promote-tested-bytes invariant

Publication must not silently create a second set of package bytes.

The CI contract is:

1. the build job sets the effective immutable version;
2. the reactor is built/tested and the aggregate SBOM is generated;
3. the published JARs are rebuilt once under identical explicit inputs and must compare byte-for-byte;
4. the deployment helper is exercised against a disposable local Maven repository and deployed JARs must compare byte-for-byte with the tested reactor JARs;
5. the build job uploads the exact tested JARs plus version-set POMs;
6. the write-capable publish job downloads those inputs and deploys them without invoking a second reactor build;
7. release/static-repository paths use the same tested inputs/evidence.

A change that reintroduces rebuilding in the publish job violates the artifact contract even if Maven coordinates remain the same.

## Version policy

Maven does not require `SNAPSHOT`. SupraCraft Bridge uses:

- source/development line: `0.1.0-dev`
- immutable main-branch CI build: `0.1.0-dev.<github-run-number>`
- release candidate: `0.1.0-rc.<n>`
- release: `0.1.0`

Do not publish pseudo-snapshot versions such as `0.1.0-SNAPSHOT.123`; Maven treats them as ordinary unique versions while the name falsely implies snapshot semantics.

The exact Git SHA belongs in provenance metadata, not in the version string. Consumers should pin exact Bridge versions for reproducible/release builds.

## Upstream relationship

Do not encode upstream ownership in the published group ID. Preserve upstream attribution in README/license/source metadata and record upstream lineage independently from SupraCraft artifact version/identity.
