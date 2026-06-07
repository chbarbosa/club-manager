import { expect, test } from '@playwright/test'

test('admin can create, search, view, and deactivate a trainer', async ({ page }) => {
  const suffix = Date.now().toString()
  const trainerName = `Carlos Mendes ${suffix}`
  const trainerEmail = `carlos.${suffix}@club.com`

  page.on('dialog', async (dialog) => dialog.accept())

  await page.goto('/login')
  await page.getByLabel('Username').fill('admin')
  await page.getByLabel('Password').fill('admin123')
  await page.getByRole('button', { name: 'Login' }).click()

  await page.getByRole('navigation').getByRole('link', { name: 'Trainers' }).click()
  await page.getByRole('button', { name: 'Add Trainer' }).click()

  await page.getByLabel('Name').fill(trainerName)
  await page.getByLabel('Email').fill(trainerEmail)
  await page.getByLabel('Date the trainer joined this club').fill('2018-06-01')
  await page.getByRole('button', { name: 'Save trainer' }).click()

  await expect(page.getByText('Trainer created.')).toBeVisible()

  await page.getByLabel('Search trainers').fill(trainerName)
  await expect(page.getByRole('cell', { name: trainerName })).toBeVisible()

  await page.locator('tr').filter({ hasText: trainerName }).getByRole('link', { name: 'View' }).click()
  await expect(page.getByRole('heading', { name: trainerName })).toBeVisible()
  await expect(page.getByText(trainerEmail)).toBeVisible()

  await page.getByRole('button', { name: 'Deactivate' }).click()
  await expect(page.getByText('Trainer deactivated.')).toBeVisible()
  await expect(page.getByText('Inactive')).toBeVisible()
})
