import api from './axios.js'

export function getClub() {
  return api.get('/club').then((response) => response.data)
}

