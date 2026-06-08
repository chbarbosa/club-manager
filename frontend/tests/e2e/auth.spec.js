import { expect, test } from '@playwright/test'

test('admin can log in and log out', async ({ page }) => {
  await page.goto('/login')
  await page.getByLabel('Username').fill('admin')
  await page.getByLabel('Password').fill('admin123')
  await page.getByRole('button', { name: 'Login' }).click()

  await expect(page).toHaveURL('/dashboard')
  await expect(page.locator('.navbar-brand')).not.toHaveText('')

  await page.getByRole('button', { name: 'Logout' }).click()
  await page.goto('/settings/club')
  await expect(page).toHaveURL('/login')
})
