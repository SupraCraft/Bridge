#!/usr/bin/env bash
set -euo pipefail

: "${JDK26_HOME:?JDK26_HOME must point to a JDK 26 installation}"

fixture_dir='compatibility/fixtures/modern-project'
source_root='compatibility/fixtures/modern-sources'
input_root='build/modern-bytecode-inputs'
result_root='build/modern-bytecode-compatibility'

host_feature="$(java -XshowSettings:properties -version 2>&1 | awk -F= '/java.specification.version/{gsub(/[[:space:]]/, "", $2); print $2; exit}')"
test -n "$host_feature"

rm -rf "$input_root" "$result_root"
mkdir -p "$input_root/java21" "$input_root/java25" "$input_root/module21" "$result_root"

"$JDK26_HOME/bin/javac" --release 21 \
  -d "$input_root/java21" \
  "$source_root/java21/ModernFixture.java"

"$JDK26_HOME/bin/javac" --release 25 \
  -d "$input_root/java25" \
  "$source_root/java25/FlexibleConstructorFixture.java"

"$JDK26_HOME/bin/javac" --release 21 \
  -d "$input_root/module21" \
  "$source_root/module21/module-info.java" \
  "$source_root/module21/modern/module/ModuleFixture.java"

printf 'Installing Bridge 0.1.0-dev under host JDK %s\n' "$host_feature"
./mvnw -B -pl bridge-plugin -am install -DskipTests

transform_case() {
  local name="$1"
  local input="$2"
  local main="$3"
  local expected="$4"
  local mode="${5:-classpath}"

  printf 'Transforming modern fixture %s under host JDK %s\n' "$name" "$host_feature"
  rm -rf "$fixture_dir/target"
  mkdir -p "$fixture_dir/target/classes"
  cp -a "$input/." "$fixture_dir/target/classes/"

  ./mvnw -B -f "$fixture_dir/pom.xml" \
    io.github.supracraft.bridge:bridge-plugin:0.1.0-dev:bridge \
    -Dbridge.flags=FORCE_COMPILE

  python3 scripts/check-run-report.py "$fixture_dir/target/bridge/bridge-report.json"
  "$JDK26_HOME/bin/java" scripts/VerifyClassFiles.java "$fixture_dir/target/classes"

  local output
  if [[ "$mode" == 'module' ]]; then
    output="$($JDK26_HOME/bin/java -Xverify:all -p "$fixture_dir/target/classes" -m "$main")"
  else
    output="$($JDK26_HOME/bin/java -Xverify:all -cp "$fixture_dir/target/classes" "$main")"
  fi
  test "$output" = "$expected"
}

transform_case java21 "$input_root/java21" modern.ModernFixture modern-ok

modern_classes="$fixture_dir/target/classes"
"$JDK26_HOME/bin/javap" -v -classpath "$modern_classes" 'modern.ModernFixture$Point' > /tmp/bridge-modern-record.txt
grep -F 'Record:' /tmp/bridge-modern-record.txt
"$JDK26_HOME/bin/javap" -v -classpath "$modern_classes" 'modern.ModernFixture$Shape' > /tmp/bridge-modern-sealed.txt
grep -F 'PermittedSubclasses:' /tmp/bridge-modern-sealed.txt
"$JDK26_HOME/bin/javap" -v -classpath "$modern_classes" modern.ModernFixture > /tmp/bridge-modern-root.txt
grep -F 'InvokeDynamic' /tmp/bridge-modern-root.txt

transform_case java25-flexible-constructor "$input_root/java25" modern25.FlexibleConstructorFixture flexible-constructor-ok

flex_classes="$fixture_dir/target/classes"
"$JDK26_HOME/bin/javap" -c -classpath "$flex_classes" modern25.FlexibleConstructorFixture > /tmp/bridge-flexible-constructor.txt
abs_line="$(grep -n -m1 'java/lang/Math.abs' /tmp/bridge-flexible-constructor.txt | cut -d: -f1)"
super_line="$(grep -n -m1 'FlexibleBase.*<init>' /tmp/bridge-flexible-constructor.txt | cut -d: -f1)"
test -n "$abs_line" -a -n "$super_line" -a "$abs_line" -lt "$super_line"

transform_case java21-module "$input_root/module21" 'bridge.fixture.modern/modern.module.ModuleFixture' module-ok module

module_classes="$fixture_dir/target/classes"
"$JDK26_HOME/bin/javap" -v "$module_classes/module-info.class" > /tmp/bridge-modern-module.txt
grep -F 'Module:' /tmp/bridge-modern-module.txt

python3 - "$host_feature" "$result_root/host-$host_feature.json" <<'PY'
import json
import pathlib
import sys

host = int(sys.argv[1])
out = pathlib.Path(sys.argv[2])
report = {
    "schema": "bridge-modern-bytecode/1",
    "bridgeVersion": "0.1.0-dev",
    "hostJavaFeature": host,
    "producerJavaFeature": 26,
    "cases": [
        {
            "id": "java21-modern-language-bytecode",
            "release": 21,
            "features": ["record", "sealed-types", "record-pattern-switch", "invokedynamic-lambda", "invokedynamic-string-concat"],
            "bridgeTransform": "pass",
            "classFileApiVerify": "pass",
            "jvmVerifyAll": "pass",
            "execution": "pass"
        },
        {
            "id": "java25-flexible-constructor",
            "release": 25,
            "features": ["statements-before-super-constructor-invocation"],
            "bridgeTransform": "pass",
            "classFileApiVerify": "pass",
            "jvmVerifyAll": "pass",
            "execution": "pass"
        },
        {
            "id": "java21-module-info",
            "release": 21,
            "features": ["module-attribute", "module-info-class"],
            "bridgeTransform": "pass",
            "classFileApiVerify": "pass",
            "jvmVerifyAll": "pass",
            "execution": "pass"
        }
    ]
}
out.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
print(f"Wrote {out}")
PY

printf 'Bridge modern-bytecode compatibility OK under host JDK %s\n' "$host_feature"
