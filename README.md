# Bridge

[![Build Status](https://github.com/SupraCraft/Bridge/actions/workflows/build.yml/badge.svg)](https://github.com/SupraCraft/Bridge/actions/workflows/build.yml)

Bridge is a post-compile Maven plugin and supporting bytecode library that adds bytecode-level language features using existing Java semantics.

This is the SupraCraft-maintained fork of [ME1312/Bridge](https://github.com/ME1312/Bridge). Upstream authorship and license history are preserved, while new artifacts identify SupraCraft as their producer.

## Repository map

- `bridge` — runtime/API surface used by transformed consumers
- `bridge-asm` — ASM hierarchy/type/visitor helpers
- `bridge-plugin` — Maven plugin that performs Bridge transformations
- `bridge-test` — reactor test artifact
- `bridge-mc-it` — optional Minecraft integration-test module, activated by the `minecraft-it` profile
- `scripts/` — reproducibility, staging, and tested-artifact publication helpers
- `PROJECT_CONTRACT.json` — compact machine-readable operational contract
- `AGENTS.md` — automated-agent rules

## Canonical artifact identity

Current Maven coordinates are SupraCraft-owned:

```text
io.github.supracraft.bridge:bridge-parent:<version>
io.github.supracraft.bridge:bridge:<version>
io.github.supracraft.bridge:bridge-asm:<version>
io.github.supracraft.bridge:bridge-plugin:<version>
io.github.supracraft.bridge:bridge-test:<version>
```

Historical `net.ME1312.ASM` coordinates belong to upstream/history and are not valid identity for new SupraCraft publications. Java packages remain `bridge.*` because they are neutral API names and changing them would create unnecessary compatibility churn.

Standalone Actions/release files use producer-qualified names:

```text
supracraft-bridge-<version>.jar
supracraft-bridge-asm-<version>.jar
supracraft-bridge-plugin-<version>.jar
supracraft-bridge-sbom-<version>.json
```

Maven repository filenames remain conventional Maven artifactId/version names.

See `ARTIFACT_IDENTITY.md`, `VERSIONING.md`, and `docs/artifact-consumption.md`.

## Version semantics

- checked-in development source line: `X.Y.Z-dev` (currently `0.1.1-dev`)
- normal CI publication: immutable `X.Y.Z-dev.<github-run-number>`
- release candidate: `X.Y.Z-rc.N`
- current stable release/tag: `0.1.0` / `v0.1.0`
- Maven `SNAPSHOT` semantics are intentionally not used

Consumers should pin one exact Bridge version across API/helper/plugin modules and record that coordinate in their own provenance.

## Toolchain and build

The repository pins Apache Maven `3.9.16` with Maven Wrapper `3.3.4` and emits Java 21 bytecode.

Use the wrapper rather than a system Maven:

```sh
./mvnw -B verify
```

Windows:

```powershell
.\mvnw.cmd -B verify
```

The build also generates the aggregate CycloneDX SBOM and exercises the reproducibility/publication contracts in CI.

## Features

Upstream feature documentation remains applicable to the shared Bridge API and transformation model:

- [constructor, method, and field redirection](https://github.com/ME1312/Bridge/wiki/Features#bridges) with `@Bridge`
- [unsafe native references](https://github.com/ME1312/Bridge/wiki/Features#invocations) with `Invocation`
- [unrestricted jumps](https://github.com/ME1312/Bridge/wiki/Features#jumps) with `Label` and `Jump`
- [multi-release class forking](https://github.com/ME1312/Bridge/wiki/Features#forks) with `Invocation.LANGUAGE_LEVEL`
- [unchecked casting, throwing, and handling](https://github.com/ME1312/Bridge/wiki/Features#unchecked) with `Unchecked`
- [class hierarchy modification](https://github.com/ME1312/Bridge/wiki/Features#type-adoption) with `@Adopt`
- [synthetic implementation hiding](https://github.com/ME1312/Bridge/wiki/Features#appending-the-synthetic-modifier) with `@Synthetic`
- optional stripping of debug metadata

## Maven consumer quickstart

GitHub Packages repository:

```text
https://maven.pkg.github.com/SupraCraft/Bridge
```

GitHub Packages requires authentication. Use `GITHUB_TOKEN` in Actions or a PAT with `read:packages` locally.

Use an **exact published version** in place of `<version>`; `0.1.0` is the current stable release:

```xml
<properties>
    <bridge.version>&lt;version&gt;</bridge.version>
</properties>

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

<dependencies>
    <dependency>
        <groupId>io.github.supracraft.bridge</groupId>
        <artifactId>bridge</artifactId>
        <version>${bridge.version}</version>
        <scope>provided</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>io.github.supracraft.bridge</groupId>
            <artifactId>bridge-plugin</artifactId>
            <version>${bridge.version}</version>
            <executions>
                <execution>
                    <goals>
                        <goal>bridge</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

## Local GitHub Packages authentication

```sh
export GITHUB_TOKEN="$PAT"
export GITHUB_ACTOR="your-github-user"
```

Configure a Maven `settings.xml` server entry with the same repository ID used for GitHub Packages.

## Minecraft integration test

The optional `minecraft-it` profile exercises Bridge against an explicitly supplied Mojang-mapped server JAR:

```sh
./mvnw -P minecraft-it -Dminecraft.serverJar=/path/to/server-<version>-mapped.jar test
```

The module probes mapped Minecraft classes through Bridge transformations. Runtime remapping to obfuscated server names remains the consuming project's responsibility.

## Publication and provenance

GitHub Actions builds/tests first, proves the published JARs reproducible, and locally proves the deploy path. Development publication and the release-qualification path both promote exact tested Maven inputs rather than rebuilding in their write-capable jobs.

This means the Maven package bytes are the tested bytes.

Every Bridge JAR records at least:

```text
Implementation-Vendor: SupraCraft
Source-Repository: SupraCraft/Bridge
Upstream-Repository: ME1312/Bridge
Build-Commit: <git-sha>
Build-Ref: <git-ref>
Build-Number: <run-number>
```

CI also retains SBOM, checksums, build metadata, and reproducibility evidence. Stable release/tag paths attach the already-tested standalone assets and qualification evidence. GitHub Packages is the canonical Maven publication channel; GitHub Pages is the public documentation surface.
