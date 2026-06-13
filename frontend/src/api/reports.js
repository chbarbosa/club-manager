import api from './axios.js'

export async function exportPlayersCsv() {
  const response = await api.get('/reports/players.csv', { responseType: 'blob' })
  downloadBlob(response.data, 'players.csv')
}

export async function exportTeamRosterCsv(teamUuid) {
  const response = await api.get(`/reports/teams/${teamUuid}/roster.csv`, { responseType: 'blob' })
  downloadBlob(response.data, 'team-roster.csv')
}

export async function exportSchedulesCsv() {
  const response = await api.get('/reports/schedules.csv', { responseType: 'blob' })
  downloadBlob(response.data, 'schedules.csv')
}

export async function exportChampionshipsCsv() {
  const response = await api.get('/reports/championships.csv', { responseType: 'blob' })
  downloadBlob(response.data, 'championships.csv')
}

export async function exportEvaluationResultsCsv(evaluationUuid) {
  const response = await api.get(`/reports/evaluations/${evaluationUuid}/results.csv`, { responseType: 'blob' })
  downloadBlob(response.data, 'evaluation-results.csv')
}

export async function exportMatchAnalysisCsv(teamUuid, matchUuid) {
  const response = await api.get(`/reports/teams/${teamUuid}/matches/${matchUuid}/analysis.csv`, { responseType: 'blob' })
  downloadBlob(response.data, 'match-analysis.csv')
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
