import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { login as loginRequest } from '../api/auth.js'
import { configureAuthHandlers } from '../api/axios.js'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [token, setToken] = useState(null)
  const [adminUuid, setAdminUuid] = useState(null)
  const [userUuid, setUserUuid] = useState(null)
  const [name, setName] = useState(null)
  const [role, setRole] = useState(null)

  const login = useCallback(async (username, password) => {
    const response = await loginRequest(username, password)
    setToken(response.token)
    setAdminUuid(response.adminUuid)
    setUserUuid(response.userUuid)
    setName(response.name)
    setRole(response.role)
    return response
  }, [])

  const logout = useCallback(() => {
    setToken(null)
    setAdminUuid(null)
    setUserUuid(null)
    setName(null)
    setRole(null)
  }, [])

  const getToken = useCallback(() => token, [token])
  const isAuthenticated = useCallback(() => Boolean(token), [token])

  useEffect(() => {
    configureAuthHandlers({ getToken, onUnauthorized: logout })
  }, [getToken, logout])

  const value = useMemo(
    () => ({ login, logout, getToken, isAuthenticated, adminUuid, userUuid, name, role }),
    [login, logout, getToken, isAuthenticated, adminUuid, userUuid, name, role],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  return useContext(AuthContext)
}
