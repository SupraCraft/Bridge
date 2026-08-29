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

The retained evidence is part of the release contract. `BUILD-METADATA.properties`, `REPRODUCIBILITY.properties`, checksums, JAR manifests, qualification evidence, and publication evidence must describe the same source commit, ref, run, and exact candidate version. An internally inconsistent evidence bundle disqualifies that candidate from stable promotion even when the published JAR bytes themselves are otherwise valid.

## Immutability and failures

A published candidate is never overwritten. If publication completes, or if publication fails after any module has become visible, corrections use the next candidate number (`rc.2`, `rc.3`, and so on). A rerun that finds an existing coordinate fails closed rather than replacing bytes.

The release branch and Actions evidence are operational records; stable release tags remain `vX.Y.Z`. Candidate publication does not imply stable release readiness.

Published candidate status records live under `release-candidates/`. They preserve whether a candidate remains eligible for promotion and why a published coordinate was superseded.

## Candidate history

- `0.1.0-rc.1` was published successfully to GitHub Packages and its JAR manifests carry the correct source provenance. It is **not eligible for stable promotion** because its retained `REPRODUCIBILITY.properties` recorded unresolved CI placeholders for the build commit, ref, and run. The immutable coordinate is preserved. See `release-candidates/0.1.0-rc.1.json`.
- `0.1.0-rc.2` was built reproducibly, qualified on host JDK 21 through 26, qualified against Java 8 through 26 input class files, passed the modern-bytecode boundary cases, and was published from the tested Maven handoff. Its retained provenance is internally consistent. `SupraCraft/VanillaCord` then consumed the exact `0.1.0-rc.2` coordinate and passed its reactor, exact-coordinate artifact check, current Minecraft stable patch/integrity/boot probe, and maintained blocking regression set. The advertised Bridge feature surface also has representative automated coverage. `0.1.0-rc.2` is therefore **eligible for stable promotion**. See `release-candidates/0.1.0-rc.2.json`.

## Consumer qualification

`SupraCraft/VanillaCord` is the reference consumer. Before Bridge `X.Y.Z` can be promoted stable, VanillaCord must qualify against the exact candidate coordinate.

VanillaCord provides a push-driven qualification lane using branch `qualification/bridge/<exact-version>` plus `BRIDGE_QUALIFICATION.json`. The lane pins the exact immutable Bridge coordinate, runs VanillaCord and the blocking Minecraft compatibility scope, and emits machine-readable consumer evidence. Ordinary VanillaCord development continues to resolve only normal immutable development coordinates unless explicitly pinned.
