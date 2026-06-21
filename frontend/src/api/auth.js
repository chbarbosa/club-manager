import api from './axios.js'

export function login(username, password) {
  return api.post('/auth/login', { username, password }).then((response) => response.data)
}

export function registerAdmin(data) {
  return api.post('/auth/register', data).then((response) => response.data)
}

export function inviteTrainerAccess(trainerUuid) {
  return api.post('/trainer-access/invitations', { trainerUuid }).then((response) => response.data)
}

export function confirmTrainerPassword(data) {
  return api.post('/trainer-access/confirm-password', data).then((response) => response.data)
}

export function requestTrainerPasswordReset() {
  return api.post('/trainer-access/password-reset/request').then((response) => response.data)
}

export function confirmTrainerPasswordReset(data) {
  return api.post('/trainer-access/password-reset/confirm', data).then((response) => response.data)
}
