import api from './axios.js'

export function getFields() {
  return api.get('/fields').then((response) => response.data)
}
