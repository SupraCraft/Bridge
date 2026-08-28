# Bridge diagnostics

Bridge uses Maven's normal logging surface. It does not maintain a separate logging framework.

## Contract

Actionable Bridge warnings/errors use a stable event identifier in square brackets. Human text may improve over time; automation and agents should key on the identifier rather than exact prose.

| Event | Severity | Meaning |
| --- | --- | --- |
| `BRIDGE-W001` | warning | Resolved Bridge API version differs from `${bridge.version}`. |
| `BRIDGE-W002` | warning | Maven plugin version differs from the resolved Bridge API version. |
| `BRIDGE-W003` | warning | Maven plugin version differs from `${bridge.version}`. |
| `BRIDGE-W004` | warning | A configured flag explicitly skipped recompilation. |
| `BRIDGE-W005` | warning | An unknown recompilation flag was supplied. |
| `BRIDGE-E001` | error | Bridge transformation failed and Maven receives the underlying exception as the cause. |

Ordinary progress remains normal Maven `INFO`/`DEBUG` output and is intentionally not assigned event IDs. This keeps the stable event namespace limited to conditions that may require attention.

## Artifact identity

Version-consistency diagnostics recognize the canonical SupraCraft API coordinate `io.github.supracraft.bridge:bridge` and retain recognition of the historical upstream `net.ME1312.ASM:bridge` coordinate for compatibility. New consumers must use the canonical coordinate.

## Automation guidance

- Treat Maven's process exit code as the authoritative success/failure signal.
- Use the stable event ID for warning/error classification.
- Preserve the full surrounding Maven output and exception cause when diagnosing a failure.
- Do not infer failure from informational progress text.
