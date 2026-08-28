# Canonical Bridge artifact consumption

Canonical artifacts produced by `SupraCraft/Bridge` use Maven group `io.github.supracraft.bridge`.

Example exact development coordinate:

```text
io.github.supracraft.bridge:bridge:0.1.0-dev.34
```

Related modules use the same version:

```text
io.github.supracraft.bridge:bridge-asm:0.1.0-dev.34
io.github.supracraft.bridge:bridge-plugin:0.1.0-dev.34
```

The repository is a fork of `ME1312/Bridge`. Historical `net.ME1312.ASM` coordinates belong to upstream/history and must not be used as the identity of new SupraCraft artifacts.

GitHub Packages repository:

```text
https://maven.pkg.github.com/SupraCraft/Bridge
```

A token with `read:packages` is required by GitHub Packages clients.

Maven repository setup:

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

Consumer dependency/plugin:

```xml
<properties>
  <bridge.version>0.1.0-dev.34</bridge.version>
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

Normal development versions are immutable `X.Y.Z-dev.N`; release candidates use `X.Y.Z-rc.N`; stable releases use `X.Y.Z`. Do not use the historical `SNAPSHOT.<run>` naming scheme for new SupraCraft builds.
