import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { configureAuthHandlers } from '../api/axios.js'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [token, setToken] = useState(null)

  const login = useCallback((newToken) => setToken(newToken), [])
  const logout = useCallback(() => setToken(null), [])
  const getToken = useCallback(() => token, [token])
  const isAuthenticated = useCallback(() => Boolean(token), [token])

  useEffect(() => {
    configureAuthHandlers({ getToken, onUnauthorized: logout })
  }, [getToken, logout])

  const value = useMemo(
    () => ({ login, logout, getToken, isAuthenticated }),
    [login, logout, getToken, isAuthenticated],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  return useContext(AuthContext)
}

