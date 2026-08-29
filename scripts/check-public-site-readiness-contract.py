#!/usr/bin/env python3
"""Validate the repository-level wiring for Bridge public-site release readiness."""

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def text(path):
    value = (ROOT / path).read_text(encoding="utf-8")
    assert value.strip(), f"empty required public-site readiness file: {path}"
    return value


def main():
    required = [
        "docs/PUBLIC_SITE_RELEASE_READINESS.md",
        "docs/assets/site.css",
        "docs/assets/site.js",
        "package.json",
        "playwright.config.mjs",
        "lighthouserc.cjs",
        "tests/site/public-site.spec.mjs",
        ".github/workflows/public-site-readiness.yml",
        ".github/workflows/pages.yml",
    ]
    for path in required:
        text(path)

    contract = json.loads(text("PROJECT_CONTRACT.json"))
    validation = contract["validation"]
    assert validation["public_site_builder"] == "scripts/build-public-site.py"
    assert validation["public_site_check"] == "scripts/check-public-site.py"
    assert validation["public_site_readiness_workflow"] == ".github/workflows/public-site-readiness.yml"
    assert validation["public_site_playwright_config"] == "playwright.config.mjs"
    assert validation["public_site_browser_test"] == "tests/site/public-site.spec.mjs"
    assert validation["public_site_accessibility_engine"] == "@axe-core/playwright"
    assert validation["public_site_lighthouse_config"] == "lighthouserc.cjs"
    assert validation["public_site_link_checker"] == "lycheeverse/lychee-action"
    assert validation["browser_projects"] == [
        "desktop-chromium",
        "desktop-firefox",
        "desktop-webkit",
        "android-chromium",
        "iphone-webkit",
    ]
    assert validation["accessibility_target"] == "WCAG 2.2 AA"

    public = contract["public_surface"]
    assert public["readiness_policy"] == "docs/PUBLIC_SITE_RELEASE_READINESS.md"
    assert public["human_routes"] == {
        "use": "use/",
        "compatibility": "compatibility/",
        "releases": "releases/",
        "release_info": "releases/<version>/",
        "accessibility": "accessibility/",
    }
    assert contract["brand"]["runtime_dependency_on_private_repo"] is False

    package = json.loads(text("package.json"))
    deps = package["devDependencies"]
    assert deps["@axe-core/playwright"] == "4.13.0"
    assert deps["@lhci/cli"] == "0.15.1"
    assert deps["@playwright/test"] == "1.62.1"

    playwright = text("playwright.config.mjs")
    for project in validation["browser_projects"]:
        assert project in playwright
    assert "--base-url ${baseURL}" in playwright

    spec = text("tests/site/public-site.spec.mjs")
    assert "expectedBasePath" in spec
    assert "current.pathname.startsWith(expectedBasePath)" in spec
    for route in ("/use/", "/compatibility/", "/releases/", "/accessibility/"):
        assert route in spec
    assert "320px reflow" in spec
    assert "AxeBuilder" in spec
    assert "primary Bridge user journeys" in spec

    lighthouse = text("lighthouserc.cjs")
    for score in (
        "'categories:accessibility': ['error', { minScore: 1 }]",
        "'categories:best-practices': ['error', { minScore: 0.95 }]",
        "'categories:seo': ['error', { minScore: 0.9 }]",
        "'categories:performance': ['error', { minScore: 0.9 }]",
    ):
        assert score in lighthouse
    assert "chromeFlags: '--no-sandbox'" in lighthouse

    readiness = text(".github/workflows/public-site-readiness.yml")
    pages = text(".github/workflows/pages.yml")
    for workflow in (readiness, pages):
        assert "npm install --ignore-scripts --no-audit --no-fund" in workflow
        assert "npx playwright install --with-deps chromium firefox webkit" in workflow
        assert "npm run site:test" in workflow
        assert "npm run site:lighthouse" in workflow
        assert "lycheeverse/lychee-action@e7477775783ea5526144ba13e8db5eec57747ce8" in workflow
        assert "./build/public-site/**/*.html" in workflow
        assert "./docs/**/*.html" not in workflow
    assert "--base-url http://127.0.0.1:4173/" in readiness
    assert "--navigation-base http://127.0.0.1:4173/" in readiness
    assert 'SITE_BASE_URL: ${{ steps.deployment.outputs.page_url }}' in pages
    assert '--grep "primary Bridge user journeys"' in pages

    print("Public-site readiness wiring OK")


if __name__ == "__main__":
    main()
