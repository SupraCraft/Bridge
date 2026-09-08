# Bridge public identity

Bridge is presented as Java/JVM developer tooling. Its visual identity is deliberately separate from Minecraft-specific SupraCraft projects while retaining a small family resemblance through copper accents, geometric construction, and the same public-site information architecture.

## Visual idea

The primary mark is a long modern bridge supported by two piers. It should read as a bridge before it reads as an arch, gate, castle, or game-world object. The hero illustration connects a source-like panel to a class-file/bytecode panel across the bridge.

The artwork is flat and geometric. Avoid voxel landscapes, grass blocks, crystals, portals, particle effects, glossy fantasy rendering, and other cues that imply Bridge is primarily a Minecraft project.

## Palette

- deep teal `#102B33` — primary page background
- teal `#243F46` — structural outline
- slate `#34494F` — secondary structure
- copper `#C27742` — shared SupraCraft hardware accent
- ivory `#F2EEE5` — deck/surface and light background
- signal cyan `#58B9C6` — small transformation/data accent

These are SupraCraft project colors. Do not copy Oracle Java brand marks, exact Java trade dress, the coffee-cup logo, or Duke.

## Voice

Use short factual statements. Prefer descriptions such as “post-compile Maven plugin,” “bytecode library,” “Java 21 bytecode,” and “build-once/promote-tested-bytes.” Avoid unsupported superlatives such as “best,” “battle-tested,” “ultra-fast,” or “secure by design.”

## Asset contract

- `docs/assets/brand/icon.svg` — canonical web/project mark
- `docs/assets/brand/hero.svg` — Pages hero illustration
- `docs/assets/brand/brand.json` — machine-readable brand rules
- `bridge/resources/META-INF/supracraft/bridge/icon.svg` — identical icon resource included in the main Bridge JAR

The documentation contract verifies that the web and JAR icon masters remain byte-for-byte identical.

## Public surfaces

GitHub Pages is generated from repository source data. Core human-readable content remains usable without JavaScript. Generated endpoints expose the same canonical project information to automation and agents:

- `/project.json`
- `/github.json`
- `/brand.json`
- `/artifacts.json`
- `/llms.txt`

The site generator must derive those files from `PROJECT_CONTRACT.json`, `GITHUB_METADATA.json`, and the brand manifest rather than maintaining duplicate hand-edited values.
