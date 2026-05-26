import { createContext, useContext, useState } from 'react'
import type { UserResponse } from '../lib/types'
import { getToken, storeTokens, clearTokens } from '../lib/api'

interface AuthContextValue {
  token: string | null
  user: UserResponse | null
  login: (accessToken: string, refreshToken: string, user: UserResponse) => void
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [token, setTokenState] = useState<string | null>(getToken)
  const [user, setUser] = useState<UserResponse | null>(null)

  function login(accessToken: string, refreshToken: string, newUser: UserResponse) {
    storeTokens(accessToken, refreshToken)
    setTokenState(accessToken)
    setUser(newUser)
  }

  function logout() {
    const refreshToken = localStorage.getItem('macromind_refresh_token')
    if (refreshToken) {
      fetch('/api/v1/auth/logout', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        body: JSON.stringify({ refreshToken }),
      }).catch(() => {})
    }
    clearTokens()
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
