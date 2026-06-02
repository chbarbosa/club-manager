import { expect, test } from '@playwright/test'

test('dashboard scaffold loads with fallback club theme', async ({ page }) => {
  await page.goto('/dashboard')

  await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible()
  await expect(page.locator('.navbar-brand')).toHaveText('Club Manager')
  await expect(page.locator('html')).toHaveCSS('--club-primary', '#2d2d2d')
})

