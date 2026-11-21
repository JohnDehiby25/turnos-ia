import { Navigate, Outlet } from 'react-router-dom'
import { useSession } from '../auth/session'
import { SessionHeader } from './SessionHeader'

export function RequireSession() {
  const { session } = useSession()

  if (session.status === 'loading') {
    return (
      <main className="flex min-h-screen items-center justify-center bg-white p-6">
        <p role="status" className="text-lg font-medium text-ink">
          Cargando…
        </p>
      </main>
    )
  }

  if (session.status === 'anonymous') return <Navigate to="/login" replace />

  return (
    <div className="min-h-screen bg-white">
      <SessionHeader username={session.user.username} />
      <Outlet />
    </div>
  )
}
