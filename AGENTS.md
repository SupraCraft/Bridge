# Instructions for automated agents

This file defines the operational rules for automated agents working in `SupraCraft/Bridge`. Human-facing context is in `README.md`; `PROJECT_CONTRACT.json` is the compact machine-readable summary.

## Repository role

Bridge is a post-compile Maven transformation system and supporting bytecode library. It is independently versioned and consumed by projects such as VanillaCord.

This repository is the SupraCraft-maintained fork of `ME1312/Bridge`. Preserve upstream attribution, but new artifacts must use SupraCraft-owned identity.

## Non-negotiable identity

- Maven group: `io.github.supracraft.bridge`
- parent: `bridge-parent`
- active modules: `bridge`, `bridge-asm`, `bridge-plugin`, `bridge-test`
- optional profile module: `bridge-mc-it`
- Java packages remain `bridge.*`
- historical `net.ME1312.ASM:*` coordinates MUST NOT be reintroduced into active POMs

## Version semantics

- checked-in parent version: `X.Y.Z-dev`
- ordinary CI: immutable `X.Y.Z-dev.<github-run-number>`
- release candidate: `X.Y.Z-rc.N`
- stable release/tag: `X.Y.Z` / `vX.Y.Z`
- do not introduce Maven `SNAPSHOT` naming
- source commit/ref/run belong in provenance, not in the Maven version string

## Toolchain

- Maven Wrapper: 3.3.4
- Maven: 3.9.16
- emitted bytecode: Java 21
- use `./mvnw` or `mvnw.cmd`; do not validate repository changes with an unpinned system Maven

Ordinary validation:

```sh
./mvnw -B verify
```

## Reproducibility and publication contract

The publication path is deliberately build-once/promote-tested-bytes:

1. build/test/generate SBOM;
2. prove published JARs byte-for-byte reproducible;
3. prove deployment locally against a disposable Maven repository;
4. stage standalone artifacts and evidence;
5. upload the exact tested JARs and version-set POMs;
6. the write-capable publish job downloads and deploys those files without rebuilding the reactor.

Do not reintroduce a second build in the publication job. Maven package bytes must remain the bytes already tested by the build job.

Relevant helpers:

- `scripts/verify-reproducible-build.sh`
- `scripts/deploy-tested-artifacts.sh`
- `scripts/stage-standalone-artifacts.sh`

## Artifact/provenance contract

Every distributable JAR must retain SupraCraft producer identity and distinct upstream lineage, including:

- `Implementation-Version`
- `Implementation-Vendor: SupraCraft`
- `Build-Commit`
- `Build-Ref`
- `Build-Number`
- `Source-Repository: SupraCraft/Bridge`
- `Upstream-Repository: ME1312/Bridge`

Standalone release/Actions files are producer-qualified `supracraft-*` names; Maven repository filenames remain conventional Maven filenames.

## Dependency discipline

Bridge is infrastructure. Keep dependency and plugin changes isolated when practical, preserve Java 21 compatibility, and prove the reactor plus artifact/provenance contracts. Do not bulk-upgrade for cosmetic freshness.

ASM is a provided compile-time baseline. Changes to it should be validated against Bridge consumers and transformation tests rather than treated as a bundled runtime-library update.

## Minecraft integration

`bridge-mc-it` is optional and activated by the `minecraft-it` profile when `minecraft.serverJar` is supplied. It consumes a Mojang-mapped server JAR supplied externally. Do not commit Mojang server JARs to this repository.

Example:

```sh
./mvnw -P minecraft-it -Dminecraft.serverJar=/path/to/server-<version>-mapped.jar test
```

## Change discipline

- use scoped branches and PRs
- keep changes attributable to one failure domain when practical
- preserve upstream-flowability; do not rename neutral Java packages for branding
- prefer deterministic validation to agent judgment in CI
- do not weaken reproducibility or exact-artifact promotion to simplify publication
- do not make Maven 4 required until GA and explicit compatibility validation
- check for nested `AGENTS.md` files if introduced later

## Documentation discipline

Static documentation should describe stable contracts, not volatile latest-run numbers. Concrete development coordinates may be shown only as clearly historical examples; copy-paste instructions should use `<version>` or an exact version supplied by the caller. If documentation and executable build logic disagree, treat the mismatch as a documentation defect and correct it through review.
