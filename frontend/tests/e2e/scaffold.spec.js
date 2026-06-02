import { expect, test } from '@playwright/test'

test('dashboard loads with seeded club theme', async ({ page }) => {
  await page.goto('/dashboard')

  await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible()
  await expect(page.locator('.navbar-brand')).toHaveText('My Club')
  await expect(page.locator('html')).toHaveCSS('--club-primary', '#2d2d2d')
})

test('club settings redirect anonymous users to login', async ({ page }) => {
  await page.goto('/settings/club')

  await expect(page).toHaveURL('/login')
  await expect(page.getByRole('heading', { name: 'Login' })).toBeVisible()
})
