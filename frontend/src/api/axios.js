import axios from 'axios'

const api = axios.create({
  baseURL: '/api/v1',
})

let getToken = () => null
let onUnauthorized = () => {}

export function configureAuthHandlers(handlers) {
  getToken = handlers.getToken
  onUnauthorized = handlers.onUnauthorized
}

api.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && error.config?.url !== '/auth/login') {
      onUnauthorized()
    }
    return Promise.reject(error)
  },
)

export default api
