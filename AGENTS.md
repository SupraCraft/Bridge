# Instructions for automated agents

This repository is the standalone public source, build, package, release, and documentation projection for SupraCraft Bridge.

## Authority boundary

- This repository is **not** the normal product-development authority.
- Do not implement independent feature work or defect fixes directly here.
- Changes arrive through a reviewed constructive projection from the authoritative development plane.
- Public issues may be used for support/intake, but implementation is reconciled through the development authority.
- Do not add private evaluators, hidden expected outcomes, comprehensive fuzz/adversarial corpora, failure fingerprints, internal research, or agent-effectiveness material here.
- Public CI must operate only on this public tree and must not receive credentials that can read private development/value repositories.
- Direct emergency fixes require an explicit exception and reconciliation back into the development authority.

## Repository role

Bridge is a post-compile Maven transformation system and supporting bytecode library. It is independently versioned and consumed by projects such as VanillaCord.

Preserve upstream attribution to `ME1312/Bridge`, while SupraCraft artifacts retain SupraCraft-owned identity.

## Non-negotiable identity

- Maven group: `io.github.supracraft.bridge`
- parent: `bridge-parent`
- active modules: `bridge`, `bridge-asm`, `bridge-plugin`, `bridge-test`
- optional profile module: `bridge-mc-it`
- Java packages remain `bridge.*`
- historical `net.ME1312.ASM:*` coordinates MUST NOT be reintroduced into active POMs

## Version semantics

- checked-in parent version: `X.Y.Z-dev`
- ordinary public build: immutable `X.Y.Z-dev.<github-run-number>` evidence only; ordinary builds do not publish packages
- release candidate: `X.Y.Z-rc.N`
- stable release/tag: `X.Y.Z` / `vX.Y.Z`
- do not introduce Maven `SNAPSHOT` naming

## Toolchain and validation

Use the Maven Wrapper; do not substitute an unpinned system Maven.

```sh
./mvnw -B verify
```

Public build/release workflows must preserve build-once/promote-tested-bytes behavior. Package publication is permitted only from this repository and only through the explicit release/candidate path. Do not add a private-read dependency to make public CI pass.

## Change discipline

- treat projected source as generated/reconciled output, not a peer development branch
- preserve deterministic build, reproducibility, artifact provenance, licensing, and public-surface checks
- preserve the public package/release/Pages identities declared by the project contract
- do not weaken a validator to accommodate a projection mismatch; correct the owning source or projection instead
- check for nested `AGENTS.md` files if introduced later
