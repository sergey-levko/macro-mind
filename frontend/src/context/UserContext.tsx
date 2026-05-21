import { createContext, useContext, useState } from 'react'
import { getUserId, setUserId as persistUserId } from '../lib/api'

interface UserContextValue {
  userId: string | null
  setUserId: (id: string) => void
}

const UserContext = createContext<UserContextValue | null>(null)

export function UserProvider({ children }: { children: React.ReactNode }) {
  const [userId, setUserIdState] = useState<string | null>(getUserId)

  function setUserId(id: string) {
    persistUserId(id)
    setUserIdState(id)
  }

  return (
    <UserContext.Provider value={{ userId, setUserId }}>
      {children}
    </UserContext.Provider>
  )
}

export function useUser(): UserContextValue {
  const ctx = useContext(UserContext)
  if (!ctx) throw new Error('useUser must be used within UserProvider')
  return ctx
}
