# Bridge versioning and provenance

Bridge uses independent semantic versioning. VanillaCord and other consumers depend on an exact Bridge Maven coordinate; Bridge versions are not lockstep with consumer versions.

## Artifact identity

Current artifacts use the SupraCraft namespace:

- parent: `io.github.supracraft.bridge:bridge-parent`
- runtime API: `io.github.supracraft.bridge:bridge`
- ASM helper: `io.github.supracraft.bridge:bridge-asm`
- Maven plugin: `io.github.supracraft.bridge:bridge-plugin`
- reactor test artifact: `io.github.supracraft.bridge:bridge-test`

Historical `net.ME1312.ASM:*` coordinates identify upstream/history and are not valid identity for new SupraCraft builds. Java packages remain `bridge.*` because they are neutral API names.

## Source version

The checked-in parent POM on the normal development branch is the source of truth for the next development line and carries `X.Y.Z-dev`. Module versions inherit it. Do not encode GitHub run numbers or commit hashes in the development source POM.

The current development source line is:

```text
0.1.0-dev
```

Maven `SNAPSHOT` semantics are intentionally not used. Historical names such as `0.1.0-SNAPSHOT.25` were ordinary immutable versions with misleading terminology because they did not end in `-SNAPSHOT`.

## CI development versions

Ordinary build CI only handles development source branches. It reads the `X.Y.Z-dev` source version and appends the GitHub Actions run number:

```text
X.Y.Z-dev.<run-number>
```

Each coordinate is immutable and unique. The JAR manifest records the full source commit, ref, and run number so the Maven coordinate does not need to encode source provenance.

Stable and release-candidate publication are intentionally not routed through the ordinary build workflow; they use the release-qualification gate described below.

## Release candidates

Release candidates use immutable `X.Y.Z-rc.N` versions. The development source POM remains `X.Y.Z-dev`; release qualification changes the version only inside isolated Actions workspaces. That prevents a candidate-preparation commit from changing the continuing development identity.

A candidate is initiated from a branch named:

```text
release/X.Y.Z-rc.N
```

The branch carries `RELEASE_CANDIDATE.json`, which binds the candidate version, release line, target branch, publication channel, and immutability requirement. The branch name and manifest must agree.

The release-qualification workflow tests the exact candidate version against the exact source SHA. It covers host JDKs 21 through 26, the Java 8 through 26 input class-file envelope on boundary hosts, the risk-focused modern-bytecode fixtures, structured diagnostics, independent class verification, behavioral execution, and packaged multi-release behavior.

A non-write-capable release-build job produces the exact Maven publication inputs and proves their reproducibility. The write-capable candidate publication job runs only after both the tested build and aggregate qualification verdict pass. It downloads and promotes those already-tested bytes; it does not rebuild or re-version Bridge.

Before qualification and immediately before publication, GitHub Packages metadata is checked to ensure the candidate coordinate does not already exist. After publication, every published module is checked for that exact version. A published or partially published candidate is never overwritten. If a correction is required after any candidate coordinate becomes visible, use the next candidate number.

See `docs/release-candidates.md` for the operational contract and candidate history.

## Stable releases

Stable releases use `X.Y.Z` and tags use:

```text
vX.Y.Z
```

Stable promotion is a distinct mode of the same release-qualification workflow, not an independent tag-triggered rebuild.

A stable source is cut from the qualified development line on:

```text
stable/X.Y.Z
```

The stable branch carries `STABLE_RELEASE.json`. Unlike a candidate branch, its checked-in parent POM and machine contracts carry the exact stable `X.Y.Z` version. This makes a checkout of `vX.Y.Z` naturally reconstructable and avoids a release tag whose source still claims to be `-dev`.

The stable manifest names one immutable `X.Y.Z-rc.N` candidate status record. Stable validation fails closed unless that candidate is `published-qualified`, is explicitly eligible for stable promotion, has a passing exact-version VanillaCord consumer qualification, and has no recorded promotion blockers.

The same release matrix then qualifies the exact stable source SHA and exact `X.Y.Z` coordinate. A non-write-capable release-build job creates and proves the reproducible stable Maven handoff. Only after the full verdict passes may the stable publication job:

1. reconfirm that both the stable Maven coordinate and `vX.Y.Z` tag are unused;
2. publish the exact tested Maven inputs without rebuilding or re-versioning;
3. verify every stable module is visible;
4. create `vX.Y.Z` pointing to the exact stable source commit; and
5. create the GitHub release from the already-tested provenance bundle.

`master` remains the development line. After a stable release, it advances to the next `X.Y.Z-dev` line rather than using the stable tag as the ongoing development source.

Consumers should use one exact Bridge version across the related API/helper/plugin modules for a build. Development automation may select a newer immutable `-dev.N` build, but release qualification and produced consumers must record the exact resolved version.

## Provenance

Every Bridge JAR contains:

- `Implementation-Title`
- `Implementation-Version`
- `Implementation-Vendor: SupraCraft`
- `Build-Commit`
- `Build-Ref`
- `Build-Number`
- `Source-Repository: SupraCraft/Bridge`
- `Upstream-Repository: ME1312/Bridge`

CI also emits the aggregate CycloneDX SBOM, `BUILD-METADATA.properties`, `REPRODUCIBILITY.properties`, and `SHA256SUMS` with the standalone artifact bundle. Release qualification evidence binds both the exact source SHA and exact Bridge version.

## Reproducibility and publication

The build uses a fixed `project.build.outputTimestamp` and proves the published Bridge JARs byte-for-byte reproducible under identical explicit inputs.

Publication is build-once/promote-tested-bytes: a non-write-capable build job creates and tests the versioned JARs and POMs; the write-capable publication job deploys those files without rebuilding the reactor. This is part of the release contract, not an implementation detail.

Candidate and stable release writes are separated from ordinary development publication. Candidate publication can write packages but cannot write repository contents. Stable publication is the only release job allowed to write both packages and repository contents because it must create the immutable release tag and GitHub release.

## Compatibility policy

Breaking API/ABI changes require a semantic-version change appropriate to their compatibility impact. Consumer compatibility is expressed through dependency coordinates and tests, not by sharing version numbers with Bridge.

Maven 4 is not a required toolchain until it reaches GA and Bridge has passed an explicit compatibility lane. Current repository validation uses Maven Wrapper 3.3.4 with Maven 3.9.16 and Java 21.
