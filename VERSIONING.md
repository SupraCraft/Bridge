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

The checked-in parent POM is the source of truth for the next development line and carries `X.Y.Z-dev`. Module versions inherit it. Do not encode GitHub run numbers or commit hashes in the source POM.

The current source line is:

```text
0.1.0-dev
```

Maven `SNAPSHOT` semantics are intentionally not used. Historical names such as `0.1.0-SNAPSHOT.25` were ordinary immutable versions with misleading terminology because they did not end in `-SNAPSHOT`.

## CI development versions

Ordinary non-tagged CI reads the source POM version and appends the GitHub Actions run number:

```text
X.Y.Z-dev.<run-number>
```

Each coordinate is immutable and unique. The JAR manifest records the full source commit, ref, and run number so the Maven coordinate does not need to encode source provenance.

## Release candidates and releases

Release candidates use immutable `X.Y.Z-rc.N` versions. Stable release tags use `vX.Y.Z`, and release publication uses `X.Y.Z`.

Consumers should use one exact Bridge version across the related API/helper/plugin modules for a build. Development automation may select a newer immutable `-dev.N` build, but the produced consumer must record the resolved exact version.

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

CI also emits the aggregate CycloneDX SBOM, `BUILD-METADATA.properties`, `REPRODUCIBILITY.properties`, and `SHA256SUMS` with the standalone artifact bundle.

## Reproducibility and publication

The build uses a fixed `project.build.outputTimestamp` and proves the published Bridge JARs byte-for-byte reproducible under identical explicit inputs.

Publication is build-once/promote-tested-bytes: the build job uploads the exact tested JARs and version-set POMs; the write-capable publication job deploys those files without rebuilding the reactor. This is part of the release contract, not an implementation detail.

## Compatibility policy

Breaking API/ABI changes require a semantic-version change appropriate to their compatibility impact. Consumer compatibility is expressed through dependency coordinates and tests, not by sharing version numbers with Bridge.

Maven 4 is not a required toolchain until it reaches GA and Bridge has passed an explicit compatibility lane. Current repository validation uses Maven Wrapper 3.3.4 with Maven 3.9.16 and Java 21.
