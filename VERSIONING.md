# Bridge versioning and provenance

Bridge uses independent semantic versioning. VanillaCord and other consumers must depend on an exact Bridge Maven coordinate; Bridge versions are not lockstep with consumer versions.

## Artifact identity

New artifacts produced by this fork use the SupraCraft namespace:

- group: `io.github.supracraft.bridge`
- parent: `io.github.supracraft.bridge:bridge-parent`
- runtime API: `io.github.supracraft.bridge:bridge`
- ASM helper: `io.github.supracraft.bridge:bridge-asm`
- Maven plugin: `io.github.supracraft.bridge:bridge-plugin`

The historical `net.ME1312.ASM` coordinates identify upstream/history and are not the canonical coordinates for new SupraCraft builds. Existing packages are retained as historical records; new publication happens only under the SupraCraft group.

Java package names remain `bridge.*`. They are API names rather than Maven ownership metadata, are already neutral, and keeping them stable minimizes compatibility churn and keeps changes suitable for contribution upstream.

## Source version

The checked-in parent POM is the single source of truth for the next development line and carries `X.Y.Z-dev`. Module versions inherit it. Do not encode GitHub Actions run numbers or commit hashes in the repository POM and do not duplicate the numeric base version in workflow code.

`SNAPSHOT` is intentionally not used. Maven does not require it, and prior versions such as `0.1.0-SNAPSHOT.25` were not Maven snapshots because they did not end in `-SNAPSHOT`. They were immutable versions with misleading terminology.

## CI development versions

Ordinary non-tagged CI reads the source POM version and appends the GitHub Actions run number:

`X.Y.Z-dev.<run-number>`

Example: `0.1.0-dev.31`.

Each coordinate is immutable and unique. The JAR manifest records the full source Git commit, ref, run number, source repository, and upstream repository, so the Maven coordinate is not expected to carry source provenance by itself.

## Release candidates

Release candidates use `X.Y.Z-rc.N` and must be immutable. They are created deliberately; normal CI does not synthesize RC numbers.

## Releases

Release tags use `vX.Y.Z`. Tagged/release builds strip the leading `v` and publish Maven artifacts as `X.Y.Z`.

A consumer release must record the exact Bridge coordinate it consumed. Development automation may select the newest compatible `-dev.<run>` build, but every produced consumer artifact must retain the resolved exact version in its manifest/SBOM/build metadata.

## Provenance

Every Bridge JAR built through Maven contains:

- `Implementation-Title`
- `Implementation-Version`
- `Implementation-Vendor: SupraCraft`
- `Build-Commit`
- `Build-Ref`
- `Build-Number`
- `Source-Repository: SupraCraft/Bridge`
- `Upstream-Repository: ME1312/Bridge`

CI additionally emits a CycloneDX aggregate SBOM (`bridge-sbom.json`) and `SHA256SUMS`.

## Compatibility policy

A breaking API/ABI change requires a semantic-version change appropriate to its compatibility impact. Consumer compatibility is expressed through dependency coordinates and tests, not by sharing version numbers with Bridge. Maven coordinates identify the producer; upstream lineage is separate provenance and must not be encoded by impersonating upstream coordinates.
