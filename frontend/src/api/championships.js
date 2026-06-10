import api from './axios.js'

export function createChampionship(data) {
  return api.post('/championships', data).then((response) => response.data)
}

export function getAllChampionships(params = {}) {
  return api.get('/championships', { params }).then((response) => response.data)
}

export function getChampionship(uuid) {
  return api.get(`/championships/${uuid}`).then((response) => response.data)
}

export function updateChampionship(uuid, data) {
  return api.put(`/championships/${uuid}`, data).then((response) => response.data)
}

export function deactivateChampionship(uuid) {
  return api.patch(`/championships/${uuid}/deactivate`).then((response) => response.data)
}

export function reactivateChampionship(uuid) {
  return api.patch(`/championships/${uuid}/reactivate`).then((response) => response.data)
}
