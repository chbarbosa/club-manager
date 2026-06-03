import api from './axios.js'

export function getAllAdmins() {
  return api.get('/admins').then((response) => response.data)
}

export function deactivateAdmin(uuid) {
  return api.patch(`/admins/${uuid}/deactivate`).then((response) => response.data)
}

export function reactivateAdmin(uuid) {
  return api.patch(`/admins/${uuid}/reactivate`).then((response) => response.data)
}
