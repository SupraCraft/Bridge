import { test, expect } from '@playwright/test';

const siteBaseUrl = new URL(process.env.SITE_BASE_URL || 'http://127.0.0.1:4173/');
const routes = ['/', '/use/', '/compatibility/', '/releases/', '/accessibility/'];
const genericStandaloneName = /^(?:click here|here|more|read more|learn more|link|button|open)$/i;

function routeUrl(route) {
  return new URL(route.replace(/^\/+/, ''), siteBaseUrl).toString();
}

async function assertPrimaryTargetsAndFocus(page, label) {
  const controls = page.locator('.brand, nav[aria-label="Primary"] a, .theme-option input, a.button');
  expect(await controls.count(), `${label} should expose declared primary controls`).toBeGreaterThan(0);

  for (let index = 0; index < await controls.count(); index += 1) {
    const control = controls.nth(index);
    await expect(control, `${label} primary control ${index}`).toBeVisible();
    const geometry = await control.evaluate(node => {
      const rect = node.getBoundingClientRect();
      return { width: rect.width, height: rect.height };
    });
    expect(geometry.width, `${label} primary control ${index} width`).toBeGreaterThanOrEqual(44);
    expect(geometry.height, `${label} primary control ${index} height`).toBeGreaterThanOrEqual(44);

    if (await control.evaluate(node => node.tabIndex >= 0)) {
      await control.focus();
      await expect(control, `${label} primary control ${index} focus`).toBeFocused();
      const focusGeometry = await control.evaluate(node => {
        const rect = node.getBoundingClientRect();
        const x = Math.min(Math.max(rect.left + rect.width / 2, 0), innerWidth - 1);
        const y = Math.min(Math.max(rect.top + rect.height / 2, 0), innerHeight - 1);
        const top = document.elementFromPoint(x, y);
        return {
          inViewport: rect.bottom > 0 && rect.right > 0 && rect.top < innerHeight && rect.left < innerWidth,
          coveredAtCenter: Boolean(top && top !== node && !node.contains(top) && !top.contains(node)),
        };
      });
      expect(focusGeometry.inViewport, `${label} primary control ${index} must remain in the viewport when focused`).toBe(true);
      expect(focusGeometry.coveredAtCenter, `${label} primary control ${index} must not be obscured at its focus point`).toBe(false);
    }
  }
}

for (const route of routes) {
  test(`${route} satisfies automation-only static HCI invariants`, async ({ page }, testInfo) => {
    const response = await page.goto(routeUrl(route), { waitUntil: 'networkidle' });
    expect(response?.status(), `${route} response`).toBeLessThan(400);

    const title = (await page.title()).trim();
    expect(title.length, `${route} needs a descriptive document title`).toBeGreaterThanOrEqual(4);
    expect(title, `${route} title should identify Bridge`).toMatch(/Bridge/i);

    const h1 = page.locator('h1');
    await expect(h1, `${route} should have one page-purpose heading`).toHaveCount(1);
    expect((await h1.textContent())?.trim().length || 0, `${route} H1 should not be empty`).toBeGreaterThanOrEqual(3);

    const headings = await page.locator('h1,h2,h3,h4,h5,h6').evaluateAll(nodes => nodes.map(node => Number(node.tagName.slice(1))));
    for (let index = 1; index < headings.length; index += 1) {
      expect(headings[index] - headings[index - 1], `${route} heading levels should not skip downward`).toBeLessThanOrEqual(1);
    }

    const standaloneActions = page.locator('a[href], button');
    for (let index = 0; index < await standaloneActions.count(); index += 1) {
      const name = await standaloneActions.nth(index).evaluate(node => {
        const explicit = node.getAttribute('aria-label')?.trim();
        return (explicit || node.textContent || '').replace(/\s+/g, ' ').trim();
      });
      expect(name.length, `${route} standalone action ${index} should not be unnamed`).toBeGreaterThan(0);
      expect(genericStandaloneName.test(name), `${route} standalone action ${index} should not use a context-free generic label: ${name}`).toBe(false);
    }

    await assertPrimaryTargetsAndFocus(page, `${testInfo.project.name} ${route}`);

    const icons = page.locator('.theme-icon');
    await expect(icons, `${route} theme icons`).toHaveCount(3);
    for (let index = 0; index < await icons.count(); index += 1) {
      await expect(icons.nth(index)).toHaveAttribute('viewBox', '0 0 24 24');
      const box = await icons.nth(index).evaluate(node => {
        const rect = node.getBoundingClientRect();
        return { width: rect.width, height: rect.height };
      });
      expect(box.width, `${route} theme icon ${index} rendered width`).toBeGreaterThan(0);
      expect(box.height, `${route} theme icon ${index} rendered height`).toBeGreaterThan(0);
      expect(Math.abs(box.width / box.height - 1), `${route} theme icon ${index} should not be distorted`).toBeLessThan(0.05);
    }
  });
}
