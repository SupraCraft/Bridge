# Bridge versioning and provenance

Bridge uses independent semantic versioning. VanillaCord and other consumers must depend on an exact Bridge Maven coordinate; Bridge versions are not lockstep with consumer versions.

## Source version

The checked-in parent POM is the single source of truth for the next development line and carries `X.Y.Z-SNAPSHOT`. Module versions inherit it. Do not encode GitHub Actions run numbers or commit hashes in the repository POM, and do not duplicate the numeric base version in workflow code.

## CI versions

Ordinary non-tagged CI reads the source POM version, strips its `-SNAPSHOT` suffix, and rewrites the effective Maven version to `X.Y.Z-SNAPSHOT.<run-number>`. That coordinate is unique within this repository and is suitable for integration testing. The artifact manifest records the full source Git commit, ref, and run number, so the Maven coordinate is not expected to carry source provenance by itself.

## Release versions

Release tags use `vX.Y.Z`. Tagged/release builds strip the leading `v` and publish Maven artifacts as `X.Y.Z`. Do not publish release tags containing `SNAPSHOT`.

A consumer release must record the exact Bridge coordinate it consumed. Consumers may resolve the newest compatible CI snapshot during normal development, but a produced artifact must retain the resolved version in its SBOM/build metadata.

## Artifact identity

Every Bridge JAR built through Maven contains:

- `Implementation-Title`
- `Implementation-Version`
- `Implementation-Vendor`
- `Build-Commit`
- `Build-Ref`
- `Build-Number`

CI additionally emits a CycloneDX aggregate SBOM (`bridge-sbom.json`) and `SHA256SUMS`. Release builds attach those provenance assets beside the JARs.

Build timestamps are intentionally not added to the JAR manifest. Version, source commit, ref, and CI run identify the build without introducing avoidable manifest timestamp variance.

## Compatibility policy

A breaking API/ABI change requires a semantic-version change appropriate to its compatibility impact. Consumer compatibility is expressed through dependency coordinates and tests, not by sharing version numbers with Bridge.
