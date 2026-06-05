import api from './axios.js'

export function createTrainer(data) {
  return api.post('/trainers', data).then((response) => response.data)
}

export function getAllTrainers(params = {}) {
  return api.get('/trainers', { params }).then((response) => response.data)
}

export function getTrainer(uuid) {
  return api.get(`/trainers/${uuid}`).then((response) => response.data)
}

export function updateTrainer(uuid, data) {
  return api.put(`/trainers/${uuid}`, data).then((response) => response.data)
}

export function deactivateTrainer(uuid) {
  return api.patch(`/trainers/${uuid}/deactivate`).then((response) => response.data)
}

export function reactivateTrainer(uuid) {
  return api.patch(`/trainers/${uuid}/reactivate`).then((response) => response.data)
}
