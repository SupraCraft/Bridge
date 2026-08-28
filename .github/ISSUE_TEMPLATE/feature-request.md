---
name: Feature Request
about: Propose a scoped Bridge capability or compatibility improvement

---

## Problem

Describe the concrete limitation or repeated maintenance problem.

## Proposed outcome

Describe what should become possible or more reliable without prescribing unnecessary implementation detail.

## Affected surface

Identify the relevant area:

- Bridge API / ABI
- bytecode transformation semantics
- `bridge-asm` helpers
- Maven plugin/build lifecycle
- artifact/version/provenance/publication
- Minecraft integration fixture
- automation/documentation/operator experience

## Evidence / motivation

Explain why the feature is worth maintaining. Include consumer failures, repeated manual work, missing upstream capability, or another concrete signal when available.

## Alternatives

Describe simpler existing approaches and why they do not adequately solve the problem. Prefer established JVM/Maven/ASM mechanisms over bespoke infrastructure when possible.

## Compatibility and maintenance cost

Call out API/ABI impact, additional runtime/build/CI cost, new dependencies, publication changes, or upstream-flowability concerns.

## Acceptance criteria

List the smallest deterministic conditions that would prove the feature useful and complete.
