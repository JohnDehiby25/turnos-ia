import { createContext, useContext } from 'react'
import type { UserResponse } from '../api/types'

export type SessionState =
  | { status: 'loading' }
  | { status: 'anonymous' }
  | { status: 'authenticated'; user: UserResponse }

export interface SessionValue {
  session: SessionState
  login: (username: string, password: string) => Promise<void>
  logout: () => Promise<void>
}

export const SessionContext = createContext<SessionValue | null>(null)

export function useSession(): SessionValue {
  const value = useContext(SessionContext)
  if (!value) throw new Error('useSession debe usarse dentro de AuthProvider')
  return value
}
