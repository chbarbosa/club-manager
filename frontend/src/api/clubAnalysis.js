import api from './axios.js'

export function getCurrentClubAnalysis() {
  return api.get('/club-analysis/current').then((response) => response.data)
}

export function getClubAnalysisHistory(params = {}) {
  return api.get('/club-analysis', { params }).then((response) => response.data)
}

export function getClubAnalysis(uuid) {
  return api.get(`/club-analysis/${uuid}`).then((response) => response.data)
}
