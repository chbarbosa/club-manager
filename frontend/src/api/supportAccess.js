import api from './axios.js'

export function createSupportAccess(data) {
  return api.post('/support-access', data).then((response) => response.data)
}

export function getSupportAccesses(params = {}) {
  return api.get('/support-access', { params }).then((response) => response.data)
}

export function revokeSupportAccess(uuid) {
  return api.patch(`/support-access/${uuid}/revoke`).then((response) => response.data)
}

export function getSupportAccessViews(uuid, params = {}) {
  return api.get(`/support-access/${uuid}/views`, { params }).then((response) => response.data)
}
