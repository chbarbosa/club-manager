import api from './axios.js'

export function createPlayer(data) {
  return api.post('/players', data).then((response) => response.data)
}

export function getAllPlayers(params = {}) {
  return api.get('/players', { params }).then((response) => response.data)
}

export function getPlayer(uuid) {
  return api.get(`/players/${uuid}`).then((response) => response.data)
}

export function updatePlayer(uuid, data) {
  return api.put(`/players/${uuid}`, data).then((response) => response.data)
}

export function deactivatePlayer(uuid) {
  return api.patch(`/players/${uuid}/deactivate`).then((response) => response.data)
}

export function reactivatePlayer(uuid) {
  return api.patch(`/players/${uuid}/reactivate`).then((response) => response.data)
}
