import api from './axios.js'

export function getClub() {
  return api.get('/club').then((response) => response.data)
}

export function updateClub(data) {
  return api.put('/club', data).then((response) => response.data)
}

export function getAllSetup() {
  return api.get('/club/setup').then((response) => response.data)
}

export function getSetupByType(type) {
  return api.get(`/club/setup/${type}`).then((response) => response.data)
}

export function updateSetup(uuid, data) {
  return api.put(`/club/setup/${uuid}`, data).then((response) => response.data)
}
