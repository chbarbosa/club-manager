import { expect, test } from '@playwright/test'

test('admin can create an evaluation group and complete an event after attendance is recorded', async ({ page }) => {
  const suffix = Date.now().toString()
  const playerName = `Evaluation Player ${suffix}`
  const registrationNumber = `EVAL-${suffix}`
  const ageGroup = `Under 15 ${suffix}`
  const evaluationTitle = `Spring Tryouts ${suffix}`

  await page.goto('/login')
  await page.getByLabel('Username').fill('admin')
  await page.getByLabel('Password').fill('admin123')
  await page.getByRole('button', { name: 'Login' }).click()

  await page.getByRole('navigation').getByRole('link', { name: 'Players' }).click()
  await page.getByRole('button', { name: 'Add Player' }).click()
  await page.getByLabel('Name').fill(playerName)
  await page.getByLabel('Birth country').fill('Brazil')
  await page.getByLabel('Living country').fill('Brazil')
  await page.getByLabel('Birthdate').fill('2012-05-20')
  await page.getByLabel('Team category').selectOption('MASCULINE')
  await page.getByLabel('Registration number').fill(registrationNumber)
  await page.getByLabel('Date the player started at this club').fill('2024-06-01')
  await page.getByRole('button', { name: 'Save player' }).click()
  await expect(page.getByText('Player created.')).toBeVisible()

  await page.getByRole('navigation').getByRole('link', { name: 'Evaluations' }).click()
  await page.getByRole('button', { name: 'Add Evaluation' }).click()
  await page.getByLabel('Title').fill(evaluationTitle)
  await page.locator('#evaluation-age-group').fill(ageGroup)
  await page.locator('#evaluation-team-category').selectOption('MASCULINE')
  await page.getByRole('button', { name: 'Save evaluation' }).click()

  await expect(page.getByText('Evaluation created.')).toBeVisible()
  await expect(page.getByRole('cell', { name: evaluationTitle })).toBeVisible()

  await page.locator('tr').filter({ hasText: evaluationTitle }).getByRole('link', { name: 'View' }).click()
  await expect(page.getByRole('heading', { name: evaluationTitle })).toBeVisible()
  await expect(page.getByRole('definition').filter({ hasText: `${ageGroup} Masculine` })).toBeVisible()

  await page.getByLabel('Assign player').selectOption({ label: playerName })
  await page.getByRole('button', { name: 'Assign player' }).click()
  await expect(page.getByText('Player assigned.')).toBeVisible()

  await page.getByLabel('Place').fill('Main Field')
  await page.getByLabel('Date').fill('2026-07-01')
  await page.getByLabel('Start time').fill('18:00')
  await page.getByLabel('Duration').selectOption('90')
  await page.getByRole('button', { name: 'Add event' }).click()
  await expect(page.getByText('Event created.')).toBeVisible()

  const eventCard = page.locator('.border.rounded').filter({ hasText: 'Main Field' })
  await eventCard.getByRole('row').filter({ hasText: playerName }).getByRole('combobox').nth(0).selectOption('PRESENT')
  await eventCard.getByRole('row').filter({ hasText: playerName }).getByRole('combobox').nth(1).selectOption('SKILLED')
  await eventCard.getByRole('row').filter({ hasText: playerName }).getByRole('button', { name: 'Save' }).click()
  await expect(page.getByText('Attendance saved.')).toBeVisible()

  await eventCard.getByRole('button', { name: 'Complete event' }).click()
  await expect(page.getByText('Event completed.')).toBeVisible()
  await expect(eventCard.getByText('COMPLETED')).toBeVisible()

  await page.getByRole('navigation').getByRole('link', { name: 'Players' }).click()
  await page.getByLabel('Search players').fill(playerName)
  await page.locator('tr').filter({ hasText: playerName }).getByRole('link', { name: 'View' }).click()
  await expect(page.getByRole('heading', { name: playerName })).toBeVisible()
  await expect(page.getByRole('definition').filter({ hasText: 'Skilled' })).toBeVisible()
  await expect(page.getByRole('heading', { name: 'Skill history' })).toBeVisible()
  await expect(page.locator('tr').filter({ hasText: 'Evaluation event completed: Main Field' }).filter({ hasText: 'Skilled' })).toBeVisible()
})
