import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { login as loginRequest } from '../api/auth.js'
import { configureAuthHandlers } from '../api/axios.js'

const AuthContext = createContext(null)
const AUTH_STORAGE_KEY = 'club-manager.auth'

function readStoredAuth() {
  try {
    const raw = window.localStorage.getItem(AUTH_STORAGE_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

function storeAuth(auth) {
  window.localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(auth))
}

function clearStoredAuth() {
  window.localStorage.removeItem(AUTH_STORAGE_KEY)
}

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(() => readStoredAuth() ?? {
    token: null,
    adminUuid: null,
    userUuid: null,
    name: null,
    role: null,
    availableRoles: [],
    multipleRoles: false,
  })

  const login = useCallback(async (username, password) => {
    const response = await loginRequest(username, password)
    const nextAuth = {
      token: response.token,
      adminUuid: response.adminUuid ?? null,
      userUuid: response.userUuid ?? null,
      name: response.name ?? null,
      role: response.role ?? null,
      availableRoles: response.availableRoles ?? (response.role ? [response.role] : []),
      multipleRoles: Boolean(response.multipleRoles),
    }
    setAuth(nextAuth)
    storeAuth(nextAuth)
    return response
  }, [])

  const logout = useCallback(() => {
    setAuth({
      token: null,
      adminUuid: null,
      userUuid: null,
      name: null,
      role: null,
      availableRoles: [],
      multipleRoles: false,
    })
    clearStoredAuth()
  }, [])

  const getToken = useCallback(() => auth.token, [auth.token])
  const isAuthenticated = useCallback(() => Boolean(auth.token), [auth.token])

  useEffect(() => {
    configureAuthHandlers({ getToken, onUnauthorized: logout })
  }, [getToken, logout])

  const value = useMemo(
    () => ({
      login,
      logout,
      getToken,
      isAuthenticated,
      adminUuid: auth.adminUuid,
      userUuid: auth.userUuid,
      name: auth.name,
      role: auth.role,
      availableRoles: auth.availableRoles ?? [],
      multipleRoles: Boolean(auth.multipleRoles),
    }),
    [login, logout, getToken, isAuthenticated, auth],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  return useContext(AuthContext)
}
