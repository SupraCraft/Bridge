# Release candidates

Bridge release candidates are immutable Maven coordinates qualified by the same release gate used for development evidence.

## Candidate identity

A candidate version is `X.Y.Z-rc.N`, where `N` starts at 1. The checked-in source POM remains `X.Y.Z-dev`; the release workflow sets the candidate version in its isolated workspace before building or qualifying artifacts.

A release-candidate run is initiated from a branch named:

```text
release/X.Y.Z-rc.N
```

That branch must contain `RELEASE_CANDIDATE.json` with schema `bridge-release-candidate/1`. The manifest records the exact candidate version, release line, target branch, publication channel, and immutability requirement.

## Qualification and publication

The release-qualification workflow binds all evidence to both the exact source commit and exact candidate version. A candidate must pass:

- reproducible build and local tested-artifact promotion;
- host JVMs 21, 22, 23, 24, 25, and 26;
- Java 8 through 26 input class-file qualification on boundary hosts 21 and 26;
- modern-bytecode qualification on boundary hosts 21 and 26;
- structured Bridge run-report validation;
- JVM, ASM, JDK Class-File API, behavioral, and multi-release JAR verification.

Only after the aggregate verdict passes may the write-capable job publish the already-tested Maven inputs to GitHub Packages. The publish job does not rebuild Bridge.

Before qualification and again immediately before publication, the workflow verifies that the candidate version is absent from every Bridge Maven module. After deployment it verifies that the version is present for every published module.

## Immutability and failures

A published candidate is never overwritten. If publication completes, or if publication fails after any module has become visible, corrections use the next candidate number (`rc.2`, `rc.3`, and so on). A rerun that finds an existing coordinate fails closed rather than replacing bytes.

The release branch and Actions evidence are operational records; stable release tags remain `vX.Y.Z`. Candidate publication does not imply stable release readiness.

## Consumer qualification

`SupraCraft/VanillaCord` is the reference consumer. Before Bridge `X.Y.Z` can be promoted stable, VanillaCord must qualify against the exact candidate coordinate. Its ordinary development resolver intentionally selects only immutable `X.Y.Z-dev.N` builds, so candidate qualification must use an explicit `BRIDGE_VERSION=X.Y.Z-rc.N` pin.
