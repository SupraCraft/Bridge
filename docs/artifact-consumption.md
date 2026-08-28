# Canonical Bridge artifact consumption

Canonical artifacts produced by `SupraCraft/Bridge` use Maven group `io.github.supracraft.bridge`.

Use one exact published version consistently across related modules:

```text
io.github.supracraft.bridge:bridge:<version>
io.github.supracraft.bridge:bridge-asm:<version>
io.github.supracraft.bridge:bridge-plugin:<version>
```

The repository is a fork of `ME1312/Bridge`. Historical `net.ME1312.ASM` coordinates belong to upstream/history and must not be used as the identity of new SupraCraft artifacts.

## Repository

GitHub Packages repository:

```text
https://maven.pkg.github.com/SupraCraft/Bridge
```

GitHub Packages clients require authentication. In GitHub Actions, use an appropriate `GITHUB_TOKEN`; locally, use a PAT with `read:packages`.

## Maven repository setup

```xml
<repositories>
  <repository>
    <id>bridge-github</id>
    <url>https://maven.pkg.github.com/SupraCraft/Bridge</url>
  </repository>
</repositories>
<pluginRepositories>
  <pluginRepository>
    <id>bridge-github</id>
    <url>https://maven.pkg.github.com/SupraCraft/Bridge</url>
  </pluginRepository>
</pluginRepositories>
```

## Consumer dependency/plugin

Set `<version>` to an exact published Bridge version:

```xml
<properties>
  <bridge.version>&lt;version&gt;</bridge.version>
</properties>

<dependency>
  <groupId>io.github.supracraft.bridge</groupId>
  <artifactId>bridge</artifactId>
  <version>${bridge.version}</version>
  <scope>provided</scope>
</dependency>

<plugin>
  <groupId>io.github.supracraft.bridge</groupId>
  <artifactId>bridge-plugin</artifactId>
  <version>${bridge.version}</version>
</plugin>
```

If a consumer also uses `bridge-asm`, use the same exact Bridge version unless a deliberately tested compatibility decision says otherwise.

## Version policy

- development source: `X.Y.Z-dev`
- immutable development publication: `X.Y.Z-dev.N`
- release candidate: `X.Y.Z-rc.N`
- stable release: `X.Y.Z`

Do not use the historical `SNAPSHOT.<run>` naming scheme for new SupraCraft builds.

Normal integration automation may discover/select a newer immutable `-dev.N` coordinate, but every produced consumer artifact should record the exact coordinate it actually consumed. Release and reproducibility work should pin an explicit immutable version.

## Publication trust model

Bridge CI publishes the exact JARs already built, tested, and reproducibility-checked by the build job. The write-capable publication job does not rebuild the reactor before deploying packages. Consumers can therefore treat the published package bytes as the tested package bytes for that workflow run.

See `../ARTIFACT_IDENTITY.md`, `../VERSIONING.md`, `../AGENTS.md`, and `../PROJECT_CONTRACT.json` for the repository contract.
