import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { login as loginRequest } from '../api/auth.js'
import { configureAuthHandlers } from '../api/axios.js'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [token, setToken] = useState(null)
  const [adminUuid, setAdminUuid] = useState(null)
  const [name, setName] = useState(null)

  const login = useCallback(async (username, password) => {
    const response = await loginRequest(username, password)
    setToken(response.token)
    setAdminUuid(response.adminUuid)
    setName(response.name)
    return response
  }, [])

  const logout = useCallback(() => {
    setToken(null)
    setAdminUuid(null)
    setName(null)
  }, [])

  const getToken = useCallback(() => token, [token])
  const isAuthenticated = useCallback(() => Boolean(token), [token])

  useEffect(() => {
    configureAuthHandlers({ getToken, onUnauthorized: logout })
  }, [getToken, logout])

  const value = useMemo(
    () => ({ login, logout, getToken, isAuthenticated, adminUuid, name }),
    [login, logout, getToken, isAuthenticated, adminUuid, name],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  return useContext(AuthContext)
}
