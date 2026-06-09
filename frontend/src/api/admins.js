import api from './axios.js'

export function getAllAdmins(params = {}) {
  return api.get('/admins', { params }).then((response) => response.data)
}

export function deactivateAdmin(uuid) {
  return api.patch(`/admins/${uuid}/deactivate`).then((response) => response.data)
}

export function reactivateAdmin(uuid) {
  return api.patch(`/admins/${uuid}/reactivate`).then((response) => response.data)
}
