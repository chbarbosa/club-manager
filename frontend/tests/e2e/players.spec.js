import { expect, test } from '@playwright/test'

test('admin can create, search, view, and deactivate a player', async ({ page }) => {
  const suffix = Date.now().toString()
  const playerName = `Joao Silva ${suffix}`

  page.on('dialog', async (dialog) => dialog.accept())

  await page.goto('/login')
  await page.getByLabel('Username').fill('admin')
  await page.getByLabel('Password').fill('admin123')
  await page.getByRole('button', { name: 'Login' }).click()

  await page.getByRole('navigation').getByRole('link', { name: 'Players' }).click()
  await page.getByRole('button', { name: 'Add Player' }).click()

  await page.getByLabel('Name').fill(playerName)
  await page.getByLabel('Birth country').fill('Brazil')
  await page.getByLabel('Living country').fill('Brazil')
  await page.getByLabel('Birthdate').fill('2005-03-15')
  await page.getByLabel('Team category').selectOption('MASCULINE')
  await page.getByLabel('Midfield').check()
  await page.getByLabel('Registration number').fill(`REG-${suffix}`)
  await page.getByLabel('Date the player started at this club').fill('2020-01-01')
  await page.getByRole('button', { name: 'Save player' }).click()

  await expect(page.getByText('Player created.')).toBeVisible()

  await page.getByLabel('Search players').fill(playerName)
  await expect(page.getByRole('cell', { name: playerName })).toBeVisible()

  await page.locator('tr').filter({ hasText: playerName }).getByRole('link', { name: 'View' }).click()
  await expect(page.getByRole('heading', { name: playerName })).toBeVisible()
  await expect(page.getByText(`REG-${suffix}`)).toBeVisible()
  await expect(page.getByText('Midfield')).toBeVisible()

  await page.getByRole('button', { name: 'Deactivate' }).click()
  await expect(page.getByText('Player deactivated.')).toBeVisible()
  await expect(page.getByText('Inactive')).toBeVisible()

  await page.getByRole('navigation').getByRole('link', { name: 'Players' }).click()
  await page.getByLabel('Search players').fill(playerName)
  await expect(page.getByRole('cell', { name: playerName })).toHaveCount(0)
  await page.getByLabel('Show inactive players too').check()
  await expect(page.getByRole('cell', { name: playerName })).toBeVisible()
})
