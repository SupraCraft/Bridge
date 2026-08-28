# Bridge ASM

[![Build Status](https://github.com/SupraCraft/Bridge/actions/workflows/build.yml/badge.svg)](https://github.com/SupraCraft/Bridge/actions/workflows/build.yml)

Bridge is a post-compile Maven plugin that adds bytecode-level language features using existing Java semantics.

> Fork identity: this is the SupraCraft-maintained fork of [ME1312/Bridge](https://github.com/ME1312/Bridge). Upstream authorship and license history are preserved, while artifacts built here use SupraCraft-owned Maven coordinates and explicit source/upstream provenance.

## Canonical artifact identity

New artifacts published from this repository use:

```text
io.github.supracraft.bridge:bridge
io.github.supracraft.bridge:bridge-asm
io.github.supracraft.bridge:bridge-plugin
```

Historical `net.ME1312.ASM` coordinates identify upstream/history and are not the canonical identity of new SupraCraft builds. Java packages remain `bridge.*` because they are already neutral API names and keeping them stable reduces compatibility churn and keeps changes practical to contribute upstream.

Development versions are immutable `X.Y.Z-dev.N`, release candidates are `X.Y.Z-rc.N`, and stable releases are `X.Y.Z`. The first canonical master publication after the coordinate migration was `0.1.0-dev.34`.

See [VERSIONING.md](VERSIONING.md), [ARTIFACT_IDENTITY.md](ARTIFACT_IDENTITY.md), and [docs/artifact-consumption.md](docs/artifact-consumption.md).

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

```xml
<properties>
    <bridge.version>0.1.0-dev.34</bridge.version>
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

Use one exact Bridge version across the API/helper/plugin modules for a build. Normal integration automation may select a newer immutable `-dev.N` coordinate, but produced consumers should record the exact coordinate they actually used.

## Local GitHub Packages authentication

```sh
export GITHUB_TOKEN="$PAT"
export GITHUB_ACTOR="your-github-user"
```

A Maven `settings.xml` server entry should use the repository ID configured for GitHub Packages and those credentials.

## Minecraft integration test

The optional `minecraft-it` profile exercises Bridge against a supplied Mojang-mapped server JAR:

```sh
mvn -P minecraft-it -Dminecraft.serverJar=/path/to/server-<version>-mapped.jar test
```

The module probes mapped Minecraft classes through Bridge transformations. Runtime remapping to obfuscated server names remains the consuming project's responsibility.

## Publishing

GitHub Actions publishes canonical artifacts to GitHub Packages. Non-tagged master builds produce immutable `X.Y.Z-dev.<run>` versions. A `vX.Y.Z` release tag publishes `X.Y.Z`. Release builds may also publish the static Maven repository used by GitHub Pages.

Every Bridge JAR records at least:

```text
Implementation-Vendor: SupraCraft
Source-Repository: SupraCraft/Bridge
Upstream-Repository: ME1312/Bridge
Build-Commit: <git-sha>
```

This keeps producer identity and upstream lineage separate rather than encoding upstream ownership into newly produced artifacts.
