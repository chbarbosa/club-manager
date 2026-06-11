import api from './axios.js'

export function createTeamMatch(teamUuid, data) {
  return api.post(`/teams/${teamUuid}/matches`, data).then((response) => response.data)
}

export function getTeamMatches(teamUuid) {
  return api.get(`/teams/${teamUuid}/matches`).then((response) => response.data)
}

export function getTeamMatch(teamUuid, matchUuid) {
  return api.get(`/teams/${teamUuid}/matches/${matchUuid}`).then((response) => response.data)
}

export function updateTeamMatch(teamUuid, matchUuid, data) {
  return api.put(`/teams/${teamUuid}/matches/${matchUuid}`, data).then((response) => response.data)
}

export function saveMatchPlayerAnalysis(teamUuid, matchUuid, playerUuid, data) {
  return api.put(`/teams/${teamUuid}/matches/${matchUuid}/players/${playerUuid}`, data).then((response) => response.data)
}
