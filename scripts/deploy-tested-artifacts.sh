#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 3 ]]; then
  echo "Usage: $0 <version> <repository-id> <repository-url>" >&2
  exit 2
fi

VERSION="$1"
REPOSITORY_ID="$2"
REPOSITORY_URL="$3"
DEPLOY_GOAL='org.apache.maven.plugins:maven-deploy-plugin:3.1.4:deploy-file'

require_file() {
  if [[ ! -s "$1" ]]; then
    echo "Required publication input is missing or empty: $1" >&2
    exit 1
  fi
}

deploy() {
  local file="$1"
  local pom="$2"
  require_file "$file"
  require_file "$pom"
  ./mvnw -B "$DEPLOY_GOAL" \
    -Dfile="$file" \
    -DpomFile="$pom" \
    -DrepositoryId="$REPOSITORY_ID" \
    -Durl="$REPOSITORY_URL" \
    -DgeneratePom=false
}

# The parent POM is itself the published artifact.
require_file pom.xml
./mvnw -B "$DEPLOY_GOAL" \
  -Dfile=pom.xml \
  -DpomFile=pom.xml \
  -Dpackaging=pom \
  -DrepositoryId="$REPOSITORY_ID" \
  -Durl="$REPOSITORY_URL" \
  -DgeneratePom=false

deploy "build/bridge-asm/bridge-asm-${VERSION}.jar" bridge-asm/pom.xml
deploy "build/bridge-plugin/bridge-plugin-${VERSION}.jar" bridge-plugin/pom.xml
deploy "build/bridge/bridge-${VERSION}.jar" bridge/pom.xml
deploy "build/bridge-test/bridge-test-${VERSION}.jar" bridge-test/pom.xml
