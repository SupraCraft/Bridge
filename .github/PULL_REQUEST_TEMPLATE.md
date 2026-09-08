## Summary

Describe the behavior, API, transformation, build, dependency, publication, or documentation change.

## Why

Explain the concrete problem being solved and link related issues when applicable.

## Contract impact

Check or describe every affected surface:

- [ ] Bridge API / ABI
- [ ] bytecode transformation behavior
- [ ] `bridge-asm` helper behavior
- [ ] Maven plugin behavior
- [ ] Minecraft integration fixture
- [ ] artifact identity/version/provenance
- [ ] reproducibility or build-once publication path
- [ ] documentation/agent/automation contract
- [ ] no contract-affecting change

If artifact/version/publication semantics change, update `ARTIFACT_IDENTITY.md`, `VERSIONING.md`, and `PROJECT_CONTRACT.json` as appropriate.

## Validation

List the exact checks run and their results. The normal baseline is:

```sh
./mvnw -B verify
```

For packaging/publication changes, include reproducibility and tested-artifact deployment evidence. For Minecraft integration changes, identify the exact externally supplied mapped server fixture.

Do not claim a check passed without corresponding local or CI evidence.

## Dependencies

List dependency/plugin changes and why each is needed. Keep unrelated upgrades separate when practical.

## Compatibility / upstream notes

Describe API/ABI implications, rollback concerns, consumer impact, and whether the change is suitable to flow upstream or intentionally SupraCraft-specific. Preserve SupraCraft producer identity and separate upstream lineage.
