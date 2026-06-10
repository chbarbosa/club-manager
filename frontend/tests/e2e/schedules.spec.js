import { expect, test } from '@playwright/test'

test('admin can create and cancel a team schedule', async ({ page }) => {
  const suffix = Date.now().toString()
  const trainerName = `Schedule Trainer ${suffix}`
  const teamName = `Schedule Team ${suffix}`
  const scheduleDate = '2026-07-15'

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
  await page.getByLabel('Identification').fill(teamName)
  await page.getByLabel('Age category').selectOption('U13')
  await page.getByLabel('Team category', { exact: true }).selectOption('MASCULINE')
  await page.getByLabel('Trainer', { exact: true }).selectOption({ label: trainerName })
  await page.getByRole('button', { name: 'Save team' }).click()
  await expect(page.getByText('Team created.')).toBeVisible()

  await page.getByRole('navigation').getByRole('link', { name: 'Schedules' }).click()
  await expect(page.getByRole('heading', { name: 'Schedules' })).toBeVisible()
  await page.getByLabel('Team', { exact: true }).selectOption({ label: `${teamName} Masculine` })
  await page.getByLabel('Field', { exact: true }).selectOption({ label: 'Main Field' })
  await page.getByLabel('Date', { exact: true }).fill(scheduleDate)
  await page.getByLabel('Time', { exact: true }).fill('18:30')
  await page.getByLabel('Duration').selectOption('90')
  await page.getByLabel('Type').selectOption('TRAINING')
  await page.getByLabel('Notes').fill('Bring cones')
  await page.getByRole('button', { name: 'Save schedule' }).click()

  await expect(page.getByText('Schedule created.')).toBeVisible()
  const scheduleRow = page.locator('tr').filter({ hasText: teamName }).filter({ hasText: 'Main Field' })
  await expect(scheduleRow).toBeVisible()
  await expect(scheduleRow.getByRole('cell', { name: '1.5h' })).toBeVisible()
  await expect(scheduleRow.getByRole('cell', { name: 'Training' })).toBeVisible()
  await expect(scheduleRow.getByText('Scheduled')).toBeVisible()

  await scheduleRow.getByRole('button', { name: 'Cancel' }).click()
  await expect(page.getByText('Schedule canceled.')).toBeVisible()
  await page.getByLabel('Filter by status').selectOption('CANCELED')
  const canceledRow = page.locator('tr').filter({ hasText: teamName }).filter({ hasText: 'Canceled' })
  await expect(canceledRow).toBeVisible()
})
