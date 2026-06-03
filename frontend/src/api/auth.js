import api from './axios.js'

export function login(username, password) {
  return api.post('/auth/login', { username, password }).then((response) => response.data)
}

export function registerAdmin(data) {
  return api.post('/auth/register', data).then((response) => response.data)
}

