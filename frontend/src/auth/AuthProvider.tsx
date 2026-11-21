import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { api } from '../api/client'
import { SessionContext, type SessionState, type SessionValue } from './session'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<SessionState>({ status: 'loading' })

  useEffect(() => {
    let active = true
    api
      .me()
      .then((user) => active && setSession({ status: 'authenticated', user }))
      .catch(() => active && setSession({ status: 'anonymous' }))
    return () => {
      active = false
    }
  }, [])

  const login = useCallback(async (username: string, password: string) => {
    const user = await api.login(username, password)
    setSession({ status: 'authenticated', user })
  }, [])

  const logout = useCallback(async () => {
    await api.logout().catch(() => undefined)
    setSession({ status: 'anonymous' })
  }, [])

  const value = useMemo<SessionValue>(() => ({ session, login, logout }), [session, login, logout])

  return <SessionContext.Provider value={value}>{children}</SessionContext.Provider>
}
