import { createContext, useContext, useState } from 'react'
import type { UserResponse } from '../lib/types'
import { getToken, setToken, clearToken } from '../lib/api'

interface AuthContextValue {
  token: string | null
  user: UserResponse | null
  login: (token: string, user: UserResponse) => void
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [token, setTokenState] = useState<string | null>(getToken)
  const [user, setUser] = useState<UserResponse | null>(null)

  function login(newToken: string, newUser: UserResponse) {
    setToken(newToken)
    setTokenState(newToken)
    setUser(newUser)
  }

  function logout() {
    clearToken()
    setTokenState(null)
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ token, user, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
