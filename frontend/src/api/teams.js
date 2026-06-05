import api from './axios.js'

export function createTeam(data) {
  return api.post('/teams', data).then((response) => response.data)
}

export function getAllTeams(params = {}) {
  return api.get('/teams', { params }).then((response) => response.data)
}

export function getTeam(uuid) {
  return api.get(`/teams/${uuid}`).then((response) => response.data)
}

export function updateTeam(uuid, data) {
  return api.put(`/teams/${uuid}`, data).then((response) => response.data)
}

export function deactivateTeam(uuid) {
  return api.patch(`/teams/${uuid}/deactivate`).then((response) => response.data)
}

export function reactivateTeam(uuid) {
  return api.patch(`/teams/${uuid}/reactivate`).then((response) => response.data)
}

