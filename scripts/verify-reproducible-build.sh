#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 4 ]]; then
  echo "Usage: $0 <version> <build-commit> <build-ref> <build-number>" >&2
  exit 2
fi

VERSION="$1"
BUILD_COMMIT="$2"
BUILD_REF="$3"
BUILD_NUMBER="$4"

ARTIFACTS=(
  "build/bridge/bridge-${VERSION}.jar"
  "build/bridge-asm/bridge-asm-${VERSION}.jar"
  "build/bridge-plugin/bridge-plugin-${VERSION}.jar"
)

TMPDIR_REPRO="$(mktemp -d)"
trap 'rm -rf "$TMPDIR_REPRO"' EXIT
mkdir -p "$TMPDIR_REPRO/first"

for artifact in "${ARTIFACTS[@]}"; do
  if [[ ! -s "$artifact" ]]; then
    echo "Missing first-build artifact: $artifact" >&2
    exit 1
  fi
  cp "$artifact" "$TMPDIR_REPRO/first/$(basename "$artifact")"
done

./mvnw -B clean verify \
  -Dbuild.commit="$BUILD_COMMIT" \
  -Dbuild.ref="$BUILD_REF" \
  -Dbuild.number="$BUILD_NUMBER"

: > REPRODUCIBILITY.properties
printf 'version=%s\n' "$VERSION" >> REPRODUCIBILITY.properties
printf 'archive.timestamp=2000-01-01T00:00:00Z\n' >> REPRODUCIBILITY.properties
printf 'build.commit=%s\n' "$BUILD_COMMIT" >> REPRODUCIBILITY.properties
printf 'build.ref=%s\n' "$BUILD_REF" >> REPRODUCIBILITY.properties
printf 'build.number=%s\n' "$BUILD_NUMBER" >> REPRODUCIBILITY.properties

for artifact in "${ARTIFACTS[@]}"; do
  name="$(basename "$artifact")"
  first="$TMPDIR_REPRO/first/$name"

  if [[ ! -s "$artifact" ]]; then
    echo "Missing clean-rebuild artifact: $artifact" >&2
    exit 1
  fi

  if ! cmp -s "$first" "$artifact"; then
    echo "Reproducibility failure: $name differs after clean rebuild." >&2
    echo "First build:" >&2
    sha256sum "$first" >&2
    echo "Second build:" >&2
    sha256sum "$artifact" >&2
    echo "First ZIP metadata:" >&2
    unzip -l "$first" >&2 || true
    echo "Second ZIP metadata:" >&2
    unzip -l "$artifact" >&2 || true
    exit 1
  fi

  sha="$(sha256sum "$artifact" | awk '{print $1}')"
  key="$(printf '%s' "$name" | tr '.-' '__')"
  printf '%s.sha256=%s\n' "$key" "$sha" >> REPRODUCIBILITY.properties
  echo "Reproducible: $name $sha"
done
