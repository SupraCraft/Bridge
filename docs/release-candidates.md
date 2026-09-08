# Release candidates and stable promotion

Bridge release candidates and stable releases are immutable Maven coordinates qualified by the same release gate. The release machinery distinguishes development, candidate, and stable modes without duplicating the compatibility matrix.

## Candidate identity

A candidate version is `X.Y.Z-rc.N`, where `N` starts at 1. The checked-in source POM remains `X.Y.Z-dev`; the release workflow sets the candidate version only in its isolated qualification workspace.

A candidate run is initiated from:

```text
release/X.Y.Z-rc.N
```

That branch must contain `RELEASE_CANDIDATE.json` with schema `bridge-release-candidate/1`. The branch name, manifest, source development line, target branch, publication channel, and immutability requirement must agree.

## Candidate qualification and publication

The release-qualification workflow binds all evidence to the exact source commit and candidate coordinate. A candidate must pass:

- reproducible build and local tested-artifact promotion;
- host JVMs 21, 22, 23, 24, 25, and 26;
- Java 8 through 26 input class-file qualification on boundary hosts 21 and 26;
- modern-bytecode qualification on boundary hosts 21 and 26;
- structured Bridge run-report validation;
- JVM, ASM, JDK Class-File API, behavioral, and multi-release JAR verification.

Only after the aggregate verdict passes may the package-write job publish the already-tested Maven handoff to GitHub Packages. It does not rebuild or re-version Bridge.

Before qualification and immediately before publication, the workflow verifies that the candidate coordinate is absent from every Bridge Maven module. After deployment it verifies that the coordinate is visible for every module.

The retained evidence is part of the release contract. `BUILD-METADATA.properties`, `REPRODUCIBILITY.properties`, checksums, JAR manifests, qualification evidence, and publication evidence must describe the same source commit, ref, run, and exact candidate version. An internally inconsistent evidence bundle disqualifies the candidate from stable promotion even if the published JAR bytes are otherwise usable.

## Candidate immutability and failures

A published candidate is never overwritten. If publication completes, or fails after any module has become visible, corrections use the next candidate number (`rc.2`, `rc.3`, and so on). A rerun that finds an existing candidate coordinate fails closed rather than replacing bytes.

Published candidate status records live under `release-candidates/`. They preserve whether a candidate remains eligible for promotion, its producer qualification, downstream consumer evidence, and any supersession reason.

## Consumer qualification

`SupraCraft/VanillaCord` is the reference consumer. Before Bridge `X.Y.Z` can be promoted stable, VanillaCord must qualify against the exact candidate coordinate.

VanillaCord provides a push-driven qualification lane using branch `qualification/bridge/<exact-version>` plus `BRIDGE_QUALIFICATION.json`. The lane pins that immutable Bridge coordinate, builds VanillaCord, verifies that the consumer artifact records the same coordinate, runs the blocking Minecraft compatibility scope, and emits machine-readable evidence. Ordinary VanillaCord development continues to resolve immutable development coordinates unless explicitly pinned.

A candidate status is stable-eligible only when this consumer verdict is present and passing in addition to the producer qualification.

## Stable source identity

Stable publication uses a dedicated source branch:

```text
stable/X.Y.Z
```

The branch carries `STABLE_RELEASE.json` with schema `bridge-stable-release/1`. Unlike a candidate branch, the checked-in parent POM and machine contracts on the stable branch carry exactly `X.Y.Z`. This makes the eventual `vX.Y.Z` tag naturally reconstructable: checking out the tag shows the released coordinate rather than the continuing development version.

The stable manifest names:

- exact stable version and `vX.Y.Z` tag;
- the exact qualified `X.Y.Z-rc.N` candidate;
- that candidate's durable status record under `release-candidates/`;
- `master` as the continuing development target;
- GitHub Packages as the publication channel;
- immutable publication semantics.

The stable validator fails closed unless the named candidate is `published-qualified`, explicitly eligible for stable promotion, has passing consumer qualification, and has no remaining promotion blockers.

## Stable qualification and publication

Stable is not a rename of candidate bytes. `X.Y.Z` is itself an exact coordinate and receives the same full producer qualification as the candidate: reproducibility, JDK 21–26 host coverage, Java 8–26 class-file inputs, modern-bytecode boundary cases, independent verification, behavioral execution, diagnostics, and multi-release behavior.

A non-write-capable build job creates and tests the exact stable Maven POM/JAR handoff. Only after the aggregate stable verdict passes may the stable publication path deploy those already-tested files. The write-capable publication path must not invoke reactor verification, rebuild Bridge, or change versions.

After package visibility is verified, the release path creates `vX.Y.Z` at the exact qualified stable source commit and publishes a GitHub Release using the already-tested standalone artifacts plus build, reproducibility, qualification, and publication evidence.

The older general build workflow is development-only. Tags and GitHub Releases do not trigger an independent stable rebuild or second publication path.

## Failure posture

Release coordinates and tags are immutable identities. Ambiguous or conflicting existing state fails closed. A stable publication must never be recovered by rebuilding different bytes under the same coordinate; any recovery must continue from the retained exact tested handoff and exact qualified source.

Because Maven publication and Git tagging are separate external operations, their evidence is retained explicitly. A package-publication failure after partial visibility requires inspection of the published modules and tested handoff before any remediation. The workflow does not silently overwrite coordinates.

## Candidate history

- `0.1.0-rc.1` was published successfully to GitHub Packages and its JAR manifests carry the correct source provenance. It is **not eligible for stable promotion** because its retained `REPRODUCIBILITY.properties` recorded unresolved CI placeholders for the build commit, ref, and run. The immutable coordinate is preserved. See `release-candidates/0.1.0-rc.1.json`.
- `0.1.0-rc.2` was built reproducibly, qualified on host JDK 21 through 26, qualified against Java 8 through 26 input class files, passed the modern-bytecode boundary cases, and was published from the tested Maven handoff. Its retained provenance is internally consistent. `SupraCraft/VanillaCord` then consumed the exact `0.1.0-rc.2` coordinate and passed its reactor, exact-coordinate artifact check, current Minecraft stable patch/integrity/boot probe, and maintained blocking regression set. The advertised Bridge feature surface also has representative automated coverage. `0.1.0-rc.2` is therefore **eligible for stable promotion**. See `release-candidates/0.1.0-rc.2.json`.
