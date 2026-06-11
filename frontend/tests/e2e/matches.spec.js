import { expect, test } from '@playwright/test'

test('admin can create a match and save player analysis', async ({ page, request }) => {
  const suffix = Date.now().toString()
  const trainerName = `Match Trainer ${suffix}`
  const teamName = `Match Team ${suffix}`
  const playerName = `Match Player ${suffix}`
  const opponentName = `Rivals ${suffix}`
  const token = await loginToken(request)
  const trainerUuid = await createTrainer(request, token, trainerName, suffix)
  const teamUuid = await createTeam(request, token, teamName, trainerUuid)
  const playerUuid = await createPlayer(request, token, playerName, `MATCH-E2E-${suffix}`)
  await assignPlayer(request, token, teamUuid, playerUuid)

  await page.goto('/login')
  await page.getByLabel('Username').fill('admin')
  await page.getByLabel('Password').fill('admin123')
  await page.getByRole('button', { name: 'Login' }).click()

  await page.getByRole('navigation').getByRole('link', { name: 'Teams' }).click()
  await page.getByLabel('Search teams').fill(teamName)
  await expect(page.getByRole('cell', { name: teamName })).toBeVisible()
  await page.locator('tr').filter({ hasText: teamName }).getByRole('link', { name: 'View' }).click()

  await expect(page.getByRole('heading', { name: `${teamName} Masculine` })).toBeVisible()
  await page.getByLabel('Opponent', { exact: true }).fill(opponentName)
  await page.getByLabel('Place').fill('Main Field')
  await page.getByLabel('Date', { exact: true }).fill('2026-09-15')
  await page.getByLabel('Time', { exact: true }).fill('18:30')
  await page.getByLabel('Team score').fill('2')
  await page.getByLabel('Opponent score').fill('1')
  await page.getByLabel('Notes').fill('Friendly match')
  await page.getByRole('button', { name: 'Save match' }).click()

  await expect(page.getByText('Match created.')).toBeVisible()
  const matchRow = page.locator('tr').filter({ hasText: opponentName })
  await expect(matchRow).toBeVisible()
  await expect(matchRow.getByText('2 - 1')).toBeVisible()
  await matchRow.getByRole('link', { name: 'Analyze' }).click()

  await expect(page.getByRole('heading', { name: 'Match analysis' })).toBeVisible()
  await expect(page.getByRole('heading', { name: playerName })).toBeVisible()
  await page.getByLabel('Improve pass').check()
  await page.getByLabel('Good passes').check()
  await page.getByLabel('Trainer notes').fill('Needs quicker passing decisions.')
  await page.getByRole('button', { name: 'Save analysis' }).click()
  await expect(page.getByText(`Analysis saved for ${playerName}.`)).toBeVisible()

  await page.getByRole('link', { name: 'Back to team' }).click()
  await page.locator('tr').filter({ hasText: opponentName }).getByRole('link', { name: 'Analyze' }).click()
  await expect(page.getByLabel('Improve pass')).toBeChecked()
  await expect(page.getByLabel('Good passes')).toBeChecked()
  await expect(page.getByLabel('Trainer notes')).toHaveValue('Needs quicker passing decisions.')
})

async function loginToken(request) {
  const response = await request.post('/api/v1/auth/login', {
    data: { username: 'admin', password: 'admin123' },
  })
  expect(response.ok()).toBeTruthy()
  return (await response.json()).token
}

async function createTrainer(request, token, name, suffix) {
  const response = await request.post('/api/v1/trainers', {
    headers: authHeaders(token),
    data: {
      name,
      birthCountry: 'Brazil',
      livingCountry: 'Brazil',
      birthdate: '1988-04-20',
      email: `match-trainer-${suffix}@club.com`,
      phone: '555-0100',
      memberSince: '2018-06-01',
    },
  })
  expect(response.ok()).toBeTruthy()
  return (await response.json()).uuid
}

async function createTeam(request, token, identification, trainerUuid) {
  const response = await request.post('/api/v1/teams', {
    headers: authHeaders(token),
    data: {
      identification,
      ageCategory: 'U13',
      teamCategory: 'MASCULINE',
      trainerUuid,
    },
  })
  expect(response.ok()).toBeTruthy()
  return (await response.json()).uuid
}

async function createPlayer(request, token, name, registrationNumber) {
  const response = await request.post('/api/v1/players', {
    headers: authHeaders(token),
    data: {
      name,
      birthCountry: 'Brazil',
      livingCountry: 'Brazil',
      birthdate: '2013-03-15',
      teamCategory: 'MASCULINE',
      positions: ['MIDFIELD'],
      registrationNumber,
      memberSince: '2020-01-01',
    },
  })
  expect(response.ok()).toBeTruthy()
  return (await response.json()).uuid
}

async function assignPlayer(request, token, teamUuid, playerUuid) {
  const response = await request.post(`/api/v1/teams/${teamUuid}/players`, {
    headers: authHeaders(token),
    data: { playerUuid },
  })
  expect(response.ok()).toBeTruthy()
}

function authHeaders(token) {
  return { Authorization: `Bearer ${token}` }
}
