#!/usr/bin/env bash
set -euo pipefail

: "${JDK26_HOME:?JDK26_HOME must point to a JDK 26 installation}"

fixture_dir='compatibility/fixtures/classfile-project'
source_file="$fixture_dir/CompatibilityFixture.java"
input_root='build/classfile-inputs'
result_root='build/classfile-compatibility'
releases=(8 11 17 21 22 23 24 25 26)

declare -A majors=(
  [8]=52
  [11]=55
  [17]=61
  [21]=65
  [22]=66
  [23]=67
  [24]=68
  [25]=69
  [26]=70
)

host_feature="$(java -XshowSettings:properties -version 2>&1 | awk -F= '/java.specification.version/{gsub(/[[:space:]]/, "", $2); print $2; exit}')"
test -n "$host_feature"

rm -rf "$input_root" "$result_root"
mkdir -p "$input_root" "$result_root"

printf 'Producing class-file fixtures with JDK 26\n'
for release in "${releases[@]}"; do
  out="$input_root/$release"
  mkdir -p "$out"
  "$JDK26_HOME/bin/javac" --release "$release" -d "$out" "$source_file"
  actual="$($JDK26_HOME/bin/javap -verbose -classpath "$out" fixture.CompatibilityFixture | awk '/major version:/{print $3; exit}')"
  test "$actual" = "${majors[$release]}"
done

printf 'Installing Bridge 0.1.0-dev under host JDK %s\n' "$host_feature"
./mvnw -B -pl bridge-plugin -am install -DskipTests

for release in "${releases[@]}"; do
  printf 'Transforming Java %s class file (major %s) under host JDK %s\n' "$release" "${majors[$release]}" "$host_feature"
  rm -rf "$fixture_dir/target"
  mkdir -p "$fixture_dir/target/classes"
  cp -a "$input_root/$release/." "$fixture_dir/target/classes/"

  ./mvnw -B -f "$fixture_dir/pom.xml" \
    io.github.supracraft.bridge:bridge-plugin:0.1.0-dev:bridge \
    -Dbridge.flags=FORCE_COMPILE

  python3 scripts/check-run-report.py "$fixture_dir/target/bridge/bridge-report.json"

  actual="$($JDK26_HOME/bin/javap -verbose -classpath "$fixture_dir/target/classes" fixture.CompatibilityFixture | awk '/major version:/{print $3; exit}')"
  test "$actual" = "${majors[$release]}"

  "$JDK26_HOME/bin/java" scripts/VerifyClassFiles.java "$fixture_dir/target/classes"
  output="$($JDK26_HOME/bin/java -Xverify:all -cp "$fixture_dir/target/classes" fixture.CompatibilityFixture)"
  test "$output" = 'compatibility-ok'
done

python3 - "$host_feature" "$result_root/host-$host_feature.json" <<'PY'
import json
import pathlib
import sys

host = int(sys.argv[1])
out = pathlib.Path(sys.argv[2])
major = {8: 52, 11: 55, 17: 61, 21: 65, 22: 66, 23: 67, 24: 68, 25: 69, 26: 70}
report = {
    "schema": "bridge-classfile-matrix/1",
    "bridgeVersion": "0.1.0-dev",
    "hostJavaFeature": host,
    "producerJavaFeature": 26,
    "targets": [
        {
            "javaRelease": release,
            "classMajor": major[release],
            "bridgeTransform": "pass",
            "bridgeRunReport": "pass",
            "classFileApiVerify": "pass",
            "jvmVerifyAll": "pass",
            "execution": "pass",
        }
        for release in (8, 11, 17, 21, 22, 23, 24, 25, 26)
    ],
}
out.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
print(f"Wrote {out}")
PY

printf 'Bridge class-file compatibility OK under host JDK %s\n' "$host_feature"
