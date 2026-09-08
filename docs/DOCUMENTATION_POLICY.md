# Documentation policy

## Purpose

Repository documentation is an operational interface for humans, automation, and agents. It must describe stable contracts accurately without turning volatile observations into apparent policy.

## Documentation layers

- `README.md` is the human entry point and explains repository role and normal workflows.
- `AGENTS.md` defines automated-agent constraints and invariants.
- `PROJECT_CONTRACT.json` is the compact machine-readable repository contract.
- `ARTIFACT_IDENTITY.md`, `VERSIONING.md`, and `docs/artifact-consumption.md` define durable focused contracts.
- Dated migration or incident records are historical evidence and must be labeled as such when they are no longer current instructions.

## Stable versus volatile information

Evergreen documentation may contain stable contracts such as artifact names, version grammar, validation commands, provenance fields, publication invariants, and repository roles.

Do not present volatile observations as evergreen facts. Examples include:

- the newest CI development build number;
- transient workflow status;
- the current newest consumer dependency coordinate;
- dependency/plugin freshness claims without an explicit review date.

Volatile facts belong in workflow output, generated artifacts, release metadata, or explicitly dated historical records.

## Source-of-truth order

When documentation disagrees with executable repository state, treat the mismatch as a defect rather than silently guessing.

For executable behavior, prefer the checked-in POM, scripts, and workflows. For stable repository policy, prefer `PROJECT_CONTRACT.json` and focused policy documents. Update all affected surfaces in the same PR when a contract changes.

## Deterministic enforcement

Run:

```sh
./scripts/check-documentation-contract.sh
```

The checker validates required documentation surfaces, parses `PROJECT_CONTRACT.json`, compares key machine-readable values with executable build configuration, and rejects known stale operational patterns.

The checker is intentionally narrow. It does not attempt natural-language truth verification; semantic review remains part of normal review.

## Change discipline

Documentation changes that alter an operational contract must identify the affected contract in the PR and include relevant validation evidence. Documentation-only changes are still expected to pass repository validation when workflows cover those files.

Historical records may mention retired names or versions when necessary to explain prior behavior, but they must be clearly historical and must not provide them as current copy-paste instructions.
