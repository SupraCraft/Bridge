# Bridge public identity

Bridge is presented as Java/JVM developer tooling. Its visual identity is deliberately separate from Minecraft-specific SupraCraft projects while retaining family resemblance through disciplined geometry, accessible public-surface behavior, and restrained project-native color.

## Visual idea

The primary mark is a long modern bridge supported by two piers. It should read as a bridge before it reads as an arch, gate, castle, or game-world object. The hero illustration connects a source-like panel to a class-file/bytecode panel across the bridge.

The **bridge silhouette is the identity anchor**. The hero and the project icon must use the same recognizable bridge shape; the hero may add source/class context around it, but the central object must still read as the same mark at a larger scale.

The artwork is flat and geometric. Avoid voxel landscapes, grass blocks, crystals, portals, particle effects, glossy fantasy rendering, and other cues that imply Bridge is primarily a Minecraft project.

## Current human-facing palette

Use a deliberately reduced three-color working palette in hero/icon artwork:

- deep teal `#102B33` — field/background and dark structure;
- ivory `#F2EEE5` — bridge surface and high-contrast structure;
- signal cyan `#58B9C6` — transformation/data accents.

Legacy/supporting palette values may remain in other established UI surfaces where needed, but new human-facing identity artwork should not use every available family color at once. Palette reduction is intended to improve silhouette recognition and visual durability, not to flatten the project into the SupraCraft organization palette.

These are SupraCraft project colors. Do not copy Oracle Java brand marks, exact Java trade dress, the coffee-cup logo, or Duke.

## Voice

Use short factual statements. Prefer descriptions such as “post-compile Maven plugin,” “bytecode library,” “Java 21 bytecode,” and “build-once/promote-tested-bytes.” Avoid unsupported superlatives such as “best,” “battle-tested,” “ultra-fast,” or “secure by design.”

## Asset contract

- `docs/assets/brand/icon.svg` — canonical web/project mark and small-scale silhouette;
- `docs/assets/brand/hero.svg` — Pages hero using the same bridge silhouette plus secondary source/class context;
- `docs/assets/brand/brand.json` — machine-readable brand rules;
- `bridge/resources/META-INF/supracraft/bridge/icon.svg` — identical icon resource included in the main Bridge JAR.

The documentation contract verifies that the web and JAR icon masters remain byte-for-byte identical. Any icon update therefore must be reconciled to the packaged resource through the normal governed projection/build path before release qualification.

## Public surfaces

GitHub Pages is generated from repository source data. Core human-readable content remains usable without JavaScript. Generated endpoints expose the same canonical project information to automation and agents:

- `/project.json`
- `/github.json`
- `/brand.json`
- `/artifacts.json`
- `/llms.txt`

The site generator must derive those files from `PROJECT_CONTRACT.json`, `GITHUB_METADATA.json`, and the brand manifest rather than maintaining duplicate hand-edited values.
