import { expect, test } from '@playwright/test'

test('admin can create, search, view, and deactivate a team', async ({ page }) => {
  const suffix = Date.now().toString()
  const trainerName = `Team Trainer ${suffix}`
  const playerName = `Team Player ${suffix}`
  const identification = `Under 13 ${suffix}`

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
  await page.getByLabel('Midfield').check()
  await page.getByLabel('Registration number').fill(`TEAM-${suffix}`)
  await page.getByLabel('Date the player started at this club').fill('2020-01-01')
  await page.getByRole('button', { name: 'Save player' }).click()
  await expect(page.getByText('Player created.')).toBeVisible()

  await page.getByRole('navigation').getByRole('link', { name: 'Teams' }).click()
  await page.getByRole('button', { name: 'Add Team' }).click()

  await page.getByLabel('Identification').fill(identification)
  await page.getByLabel('Age category').selectOption('U13')
  await page.getByLabel('Team category', { exact: true }).selectOption('MASCULINE')
  await page.getByLabel('Trainer', { exact: true }).selectOption({ label: trainerName })
  await page.getByRole('button', { name: 'Save team' }).click()

  await expect(page.getByText('Team created.')).toBeVisible()
  await page.getByLabel('Search teams').fill(identification)
  await expect(page.getByRole('cell', { name: identification })).toBeVisible()

  await page.locator('tr').filter({ hasText: identification }).getByRole('link', { name: 'View' }).click()
  await expect(page.getByRole('heading', { name: `${identification} Masculine` })).toBeVisible()
  await expect(page.getByText(trainerName)).toBeVisible()
  await expect(page.getByText('0 active players').first()).toBeVisible()
  await expect(page.getByText('No active championship associated.')).toBeVisible()
  await expect(page.getByRole('link', { name: 'Add championship' })).toBeVisible()

  await page.getByLabel('Player').selectOption({ label: playerName })
  await page.getByRole('button', { name: 'Assign player' }).click()
  await expect(page.getByText('Player assigned to team.')).toBeVisible()
  await expect(page.getByRole('cell', { name: playerName })).toBeVisible()
  await expect(page.getByText('1 active player').first()).toBeVisible()
  await expect(page.getByRole('heading', { name: 'Team composition' })).toBeVisible()
  await expect(page.getByRole('columnheader', { name: 'Goalkeepers' })).toBeVisible()
  await expect(page.getByRole('columnheader', { name: 'Midfielders' })).toBeVisible()
  await expect(page.getByRole('cell', { name: 'Midfield' })).toBeVisible()
  await expect(page.locator('tr').filter({ hasText: playerName }).getByRole('cell').nth(1)).toHaveText('13')

  await page.locator('tr').filter({ hasText: playerName }).getByRole('link', { name: playerName }).click()
  await expect(page.getByRole('heading', { name: playerName })).toBeVisible()
  await page.goBack()
  await expect(page.getByRole('heading', { name: `${identification} Masculine` })).toBeVisible()

  await page.locator('tr').filter({ hasText: playerName }).getByRole('button', { name: 'Remove' }).click()
  await expect(page.getByText('Player removed from team.')).toBeVisible()
  await expect(page.getByText('No players assigned to this team.')).toBeVisible()

  await page.getByRole('button', { name: 'Deactivate' }).click()
  await expect(page.getByText('Team deactivated.')).toBeVisible()
  await expect(page.getByText('Inactive')).toBeVisible()
})

test('admin sees composition advice when a team reaches 12 players', async ({ page, request }) => {
  const suffix = Date.now().toString()
  const trainerName = `Advice Trainer ${suffix}`
  const teamName = `Advice Team ${suffix}`
  const token = await loginToken(request)
  const trainerUuid = await createTrainer(request, token, trainerName, suffix)
  const teamUuid = await createTeam(request, token, teamName, trainerUuid)

  for (let index = 0; index < 12; index += 1) {
    const playerUuid = await createPlayer(request, token, `Advice Player ${index} ${suffix}`, `ADVICE-${suffix}-${index}`)
    await assignPlayer(request, token, teamUuid, playerUuid)
  }

  await page.goto('/login')
  await page.getByLabel('Username').fill('admin')
  await page.getByLabel('Password').fill('admin123')
  await page.getByRole('button', { name: 'Login' }).click()
  await page.getByRole('navigation').getByRole('link', { name: 'Teams' }).click()
  await page.getByLabel('Search teams').fill(teamName)
  await expect(page.getByRole('cell', { name: teamName })).toBeVisible()
  await page.locator('tr').filter({ hasText: teamName }).getByRole('link', { name: 'View' }).click()

  await expect(page.getByRole('heading', { name: `${teamName} Masculine` })).toBeVisible()
  await expect(page.getByText('12 active players').first()).toBeVisible()
  await expect(page.getByText('No goalkeepers assigned.')).toBeVisible()
  await expect(page.getByText('Few defenders assigned.')).toBeVisible()
  await expect(page.getByText('Few attackers assigned.')).toBeVisible()
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
      email: `advice-trainer-${suffix}@club.com`,
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
