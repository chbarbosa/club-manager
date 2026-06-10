import api from './axios.js'

export function createSchedule(data) {
  return api.post('/schedules', data).then((response) => response.data)
}

export function getAllSchedules(params = {}) {
  return api.get('/schedules', { params }).then((response) => response.data)
}

export function getSchedule(uuid) {
  return api.get(`/schedules/${uuid}`).then((response) => response.data)
}

export function updateSchedule(uuid, data) {
  return api.put(`/schedules/${uuid}`, data).then((response) => response.data)
}

export function cancelSchedule(uuid, cancelReason = '') {
  return api.patch(`/schedules/${uuid}/cancel`, { cancelReason }).then((response) => response.data)
}
