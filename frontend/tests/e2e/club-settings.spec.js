import { expect, test } from '@playwright/test'

test('admin updates club identity and primary colour', async ({ page }) => {
  await page.goto('/login')
  await page.getByLabel('Username').fill('admin')
  await page.getByLabel('Password').fill('admin123')
  await page.getByRole('button', { name: 'Login' }).click()

  await expect(page).toHaveURL('/dashboard')
  await page.getByRole('main').getByRole('link', { name: 'Open settings' }).click()
  await page.getByLabel('Club name').fill('My Club')
  await page.getByLabel('Primary colour').fill('#123456')
  await page.getByRole('button', { name: 'Save club settings' }).click()

  await expect(page.locator('.navbar-brand')).toHaveText('My Club')
  await expect(page.locator('html')).toHaveCSS('--club-primary', '#123456')

  await page.getByLabel('Primary colour').fill('#2d2d2d')
  await page.getByRole('button', { name: 'Save club settings' }).click()
  await expect(page.locator('html')).toHaveCSS('--club-primary', '#2d2d2d')
})
