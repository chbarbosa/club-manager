import { expect, test } from '@playwright/test'

test('admin can create a championship and see the participating team players', async ({ page }) => {
  const suffix = Date.now().toString()
  const trainerName = `Championship Trainer ${suffix}`
  const playerName = `Championship Player ${suffix}`
  const teamName = `Championship Team ${suffix}`
  const championshipName = `Spring Cup ${suffix}`

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

  await page.getByRole('navigation').getByRole('link', { name: 'Players' }).click()
  await page.getByRole('button', { name: 'Add Player' }).click()
  await page.getByLabel('Name').fill(playerName)
  await page.getByLabel('Birth country').fill('Brazil')
  await page.getByLabel('Living country').fill('Brazil')
  await page.getByLabel('Birthdate').fill('2013-03-15')
  await page.getByLabel('Team category').selectOption('MASCULINE')
  await page.getByLabel('Attack').check()
  await page.getByLabel('Registration number').fill(`CHAMP-${suffix}`)
  await page.getByLabel('Date the player started at this club').fill('2020-01-01')
  await page.getByRole('button', { name: 'Save player' }).click()
  await expect(page.getByText('Player created.')).toBeVisible()

  await page.getByRole('navigation').getByRole('link', { name: 'Teams' }).click()
  await page.getByRole('button', { name: 'Add Team' }).click()
  await page.getByLabel('Identification').fill(teamName)
  await page.getByLabel('Age category').selectOption('U13')
  await page.getByLabel('Team category', { exact: true }).selectOption('MASCULINE')
  await page.getByLabel('Trainer', { exact: true }).selectOption({ label: trainerName })
  await page.getByRole('button', { name: 'Save team' }).click()
  await expect(page.getByText('Team created.')).toBeVisible()

  await page.getByLabel('Search teams').fill(teamName)
  await expect(page.getByRole('cell', { name: teamName })).toBeVisible()
  await page.locator('tr').filter({ hasText: teamName }).getByRole('link', { name: 'View' }).click()
  await page.getByLabel('Player').selectOption({ label: playerName })
  await page.getByRole('button', { name: 'Assign player' }).click()
  await expect(page.getByText('Player assigned to team.')).toBeVisible()

  await page.getByRole('navigation').getByRole('link', { name: 'Championships' }).click()
  await expect(page.getByRole('heading', { name: 'Championships' })).toBeVisible()
  await page.getByLabel('Name').fill(championshipName)
  await page.getByLabel('Team', { exact: true }).selectOption({ label: `${teamName} Masculine` })
  await page.getByLabel('Description').fill('Regional competition')
  await page.getByLabel('Start month').selectOption('3')
  await page.getByLabel('Start year').fill('2026')
  await page.getByLabel('End month').selectOption('6')
  await page.getByLabel('End year').fill('2026')
  await page.getByRole('button', { name: 'Save championship' }).click()

  await expect(page.getByText('Championship created.')).toBeVisible()
  await page.getByLabel('Search championships').fill(championshipName)
  await page.getByRole('button', { name: 'Search' }).click()
  await expect(page.getByRole('cell', { name: championshipName })).toBeVisible()

  await page.locator('tr').filter({ hasText: championshipName }).getByRole('link', { name: 'View' }).click()
  await expect(page.getByRole('heading', { name: championshipName })).toBeVisible()
  await expect(page.getByText(`${teamName} Masculine`)).toBeVisible()
  await expect(page.getByText('The complete active team participates in this championship.')).toBeVisible()
  await expect(page.getByRole('cell', { name: playerName })).toBeVisible()
  await expect(page.getByText('1 active player')).toBeVisible()
  await expect(page.getByRole('cell', { name: 'Attack' })).toBeVisible()
})
