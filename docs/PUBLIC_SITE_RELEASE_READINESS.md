# Public-site release readiness

The Bridge GitHub Pages site is a release interface for users, developers, automation, and assistive-technology users. A successful static build is necessary but is not sufficient evidence that the public surface is ready.

## Required gates

1. **Deterministic project contract** — `scripts/check-public-site.py` validates generated routes, machine endpoints, stable-release metadata, resources, and Bridge-specific public contracts.
2. **Cross-browser user flows** — Playwright Test runs `tests/site/public-site.spec.mjs` against candidate bytes built locally by `scripts/build-public-site.py`.
3. **Automated accessibility** — `@axe-core/playwright` runs inside the browser suite in light and dark system modes.
4. **Quality budgets** — Lighthouse CI evaluates accessibility, best practices, SEO, and performance against `lighthouserc.cjs`.
5. **Link integrity** — Lychee checks repository/documentation links and generated HTML. The deterministic site checker remains authoritative for candidate-site internal routes and resources.
6. **Deployed smoke** — after Pages deployment, the deterministic checker verifies the live surface and Chromium repeats the primary Bridge user journey against the deployed `/Bridge/` base path.

The browser/accessibility/Lighthouse/link tooling is standard upstream tooling. Project code should contain Bridge-specific journeys and contracts, not a bespoke browser framework.

## Consumer-owned candidate mechanics

The PR/push candidate workflow at `.github/workflows/public-site-readiness.yml` owns its execution mechanics in this repository. It checks out the candidate, builds and validates the exact candidate site bytes, runs the local public-surface conformance validator, installs the pinned browser QA dependencies, executes the Playwright browser/accessibility matrix and Lighthouse budgets, runs the pinned Lychee action, and uploads diagnostics.

The deterministic public-surface conformance validator is vendored at `scripts/public-web/validate-surface-contract.py`. Its migration provenance is retained in the pull-request/Git history; runtime qualification no longer depends on fetching validator bytes or invoking a reusable workflow from another repository.

Bridge continues to own its exact candidate-build command, deterministic validation command, link-check paths/exclusions, `package.json`, Playwright configuration/specs, Lighthouse configuration, routes, browser/device matrix, accessibility assertions, themes, and critical Maven/use/compatibility/release journeys.

The production Pages workflow remains local and independently repeats the candidate gates before constructing/deploying the Pages artifact, then performs deterministic and real-browser smoke against the deployed site.

The consumer-owned readiness wiring and local validator identity are recorded in `PROJECT_CONTRACT.json` and enforced by `scripts/check-public-site-readiness-contract.py`, so a silent return to external runtime readiness dependencies is detectable.

## Blocking browser/device envelope

The release-readiness suite must pass in desktop Chromium, desktop Firefox, desktop WebKit/Safari-family behavior, Android Chromium using a Playwright Pixel profile, and iPhone WebKit using a Playwright iPhone profile.

A separate 320 CSS-pixel reflow assertion runs across browser projects to ensure primary navigation and controls remain usable and the document does not acquire accidental horizontal overflow.

## Browser acceptance envelope

The Playwright suite verifies that:

- every intended human route returns successfully with one H1, named primary navigation, a main landmark, footer, project identity, theme selector, skip link, and one current-page marker;
- automated axe scans report no configured WCAG A/AA violations in light and dark system modes;
- `Theme = System` follows `prefers-color-scheme`, while explicit Light/Dark choices persist locally and override the system setting;
- desktop keyboard users can focus and activate the skip link;
- the page does not overflow horizontally at 320 CSS pixels and primary navigation remains available;
- Maven/use, compatibility, release, and artifact journeys remain on friendly Pages surfaces where appropriate;
- direct artifact URLs still resolve from the stable-release contract rather than being inferred from page text;
- browser page errors and console errors are absent;
- candidate and deployed navigation remains under the complete site base path, not merely the same origin.

## Accessibility target

The public site targets **WCAG 2.2 Level AA** and is designed to align with the Revised Section 508 web accessibility criteria where applicable. Automated testing is evidence, not certification: automated tools detect only a subset of accessibility defects.

Material interaction/design changes still require manual keyboard, zoom/reflow, screen-reader/semantic, and forced-colors/high-contrast review before the surface is described as release-ready.

Use native HTML semantics before ARIA. ARIA should clarify relationships or state that HTML cannot express; it should not replace correctly structured headings, links, buttons, labels, tables, and landmarks.

## Candidate bytes, not yesterday's deployment

PR validation must test the exact candidate site bytes. `scripts/build-public-site.py --base-url <local-url>` rewrites generated navigation/assets for the local candidate server so browser tests cannot accidentally follow the currently deployed production site and certify an older revision.

Canonical links remain production-facing metadata; navigation test URLs use the candidate-local base.

GitHub Pages project sites also require base-path awareness. For Bridge, the production site root is `https://supracraft.github.io/Bridge/`, not the organization root. Browser guards therefore verify both origin and the `/Bridge/` site base path.

## Local reproduction

```sh
npm install --ignore-scripts --no-audit --no-fund
npx playwright install --with-deps chromium firefox webkit
npm run site:test
CHROME_PATH="$(node --input-type=module -e "import { chromium } from '@playwright/test'; process.stdout.write(chromium.executablePath())")" npm run site:lighthouse
```

CI also runs the pinned Lychee GitHub Action for link checking. Browser traces/screenshots, the Playwright HTML report, Lighthouse reports, and Lychee output are retained as diagnostic evidence when available.
