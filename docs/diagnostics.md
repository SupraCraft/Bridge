# Bridge diagnostics

Bridge uses Maven's normal logging surface for live human feedback. It does not maintain a separate logging framework.

In addition, Bridge emits a small machine-readable run report for automation and agents. Logging and reporting are separate concerns: Maven controls live console presentation, while the JSON report records the outcome and facts of the completed Bridge operation.

## Stable diagnostics

Actionable Bridge warnings/errors use a stable diagnostic identifier in square brackets. Human text may improve over time; automation and agents should key on the identifier rather than exact prose.

| Event | Severity | Meaning |
| --- | --- | --- |
| `BRIDGE-W001` | warning | Resolved Bridge API version differs from `${bridge.version}`. |
| `BRIDGE-W002` | warning | Maven plugin version differs from the resolved Bridge API version. |
| `BRIDGE-W003` | warning | Maven plugin version differs from `${bridge.version}`. |
| `BRIDGE-W004` | warning | A configured flag explicitly skipped recompilation. |
| `BRIDGE-W005` | warning | An unknown recompilation flag was supplied. |
| `BRIDGE-E001` | error | Bridge transformation failed and Maven receives the underlying exception as the cause. |
| `BRIDGE-E002` | error | An enabled structured run report could not be written. |

Ordinary progress remains normal Maven `INFO`/`DEBUG` output and is intentionally not assigned event IDs. This keeps the stable diagnostic namespace limited to conditions that may require attention.

## Structured run report

The default report is:

```text
target/bridge/bridge-report.json
```

The path is relative to the Maven module's build directory through the default `${project.build.directory}/bridge/bridge-report.json` configuration.

The report:

- uses schema identifier `bridge-run/1`;
- is described by `schemas/bridge-run-report.schema.json`;
- records `success`, `no-op`, `skipped`, or `failed` status;
- records Bridge, ASM, and host Java versions;
- records classes examined/transformed and transformation counts;
- records observational phase/total timing;
- records stable Bridge warning/error diagnostics;
- intentionally omits absolute paths, environment variables, usernames, hostnames, and full stack traces;
- is operational evidence only and is never part of reproducible artifact identity.

Configuration:

```text
-Dbridge.report=true|false
-Dbridge.reportFile=<path>
```

`bridge.report` defaults to `true`. If reporting is enabled and Bridge cannot write the report, the invocation fails with `BRIDGE-E002`. If transformation itself fails, Bridge attempts to write a `failed` report containing `BRIDGE-E001` before propagating the Maven failure. Maven's process exit code remains the authoritative success/failure signal.

The repository validator `scripts/check-run-report.py` validates the runtime report without third-party dependencies. Compatibility CI runs that validator against the report emitted by the real Maven plugin on each tested host JVM.

## Human output

Bridge keeps Maven's normal `INFO`/`DEBUG` behavior and therefore continues to honor Maven's normal quiet/debug controls. Bridge does not implement separate human, automation, or agent logging modes.

Each invocation ends with a concise summary containing:

- classes examined;
- classes transformed;
- bridge/invocation/adjustment/removal/fork counts;
- warning count.

Automation that does not need live human output should use Maven's existing console controls and consume the structured report rather than parsing progress prose.

## Artifact identity

Version-consistency diagnostics recognize the canonical SupraCraft API coordinate `io.github.supracraft.bridge:bridge` and retain recognition of the historical upstream `net.ME1312.ASM:bridge` coordinate for compatibility. New consumers must use the canonical coordinate.

## Automation and agent guidance

- Treat Maven's process exit code as the authoritative success/failure signal.
- Consume `bridge-report.json` for structured outcome and transformation facts.
- Use stable diagnostic IDs for warning/error classification.
- Preserve the surrounding Maven output and exception cause when deeper diagnosis is required.
- Do not infer failure from informational progress text.
- Do not treat timing fields as reproducibility identity.
