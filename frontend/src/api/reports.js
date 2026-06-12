import api from './axios.js'

export async function exportPlayersCsv() {
  const response = await api.get('/reports/players.csv', { responseType: 'blob' })
  downloadBlob(response.data, 'players.csv')
}

export async function exportTeamRosterCsv(teamUuid) {
  const response = await api.get(`/reports/teams/${teamUuid}/roster.csv`, { responseType: 'blob' })
  downloadBlob(response.data, 'team-roster.csv')
}

function downloadBlob(blob, filename) {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  link.remove()
  window.URL.revokeObjectURL(url)
}
