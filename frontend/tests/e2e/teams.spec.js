import { expect, test } from '@playwright/test'

test('admin can create, search, view, and deactivate a team', async ({ page }) => {
  const suffix = Date.now().toString()
  const trainerName = `Team Trainer ${suffix}`
  const ageGroup = `Under 13 ${suffix}`

  page.on('dialog', async (dialog) => dialog.accept())

  await page.goto('/login')
  await page.getByLabel('Username').fill('admin')
  await page.getByLabel('Password').fill('admin123')
  await page.getByRole('button', { name: 'Login' }).click()

  await page.getByRole('navigation').getByRole('link', { name: 'Trainers' }).click()
  await page.getByRole('button', { name: 'Add Trainer' }).click()
  await page.getByLabel('Name').fill(trainerName)
  await page.getByLabel('Date the trainer joined this club').fill('2018-06-01')
  await page.getByRole('button', { name: 'Save trainer' }).click()
  await expect(page.getByText('Trainer created.')).toBeVisible()

  await page.getByRole('navigation').getByRole('link', { name: 'Teams' }).click()
  await page.getByRole('button', { name: 'Add Team' }).click()

  await page.getByLabel('Age group').fill(ageGroup)
  await page.getByLabel('Team category', { exact: true }).selectOption('MASCULINE')
  await page.getByLabel('Trainer').selectOption({ label: trainerName })
  await page.getByRole('button', { name: 'Save team' }).click()

  await expect(page.getByText('Team created.')).toBeVisible()
  await page.getByLabel('Search teams').fill(ageGroup)
  await expect(page.getByRole('cell', { name: ageGroup })).toBeVisible()

  await page.locator('tr').filter({ hasText: ageGroup }).getByRole('link', { name: 'View' }).click()
  await expect(page.getByRole('heading', { name: `${ageGroup} Masculine` })).toBeVisible()
  await expect(page.getByText(trainerName)).toBeVisible()

  await page.getByRole('button', { name: 'Deactivate' }).click()
  await expect(page.getByText('Team deactivated.')).toBeVisible()
  await expect(page.getByText('Inactive')).toBeVisible()
})
