import { useState, type FormEvent } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { ApiError } from '../api/client'
import { useSession } from '../auth/session'

const CONNECTION_ERROR = 'No hay conexión con el servidor. Intenta de nuevo.'

export function LoginPage() {
  const { session, login } = useSession()
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  if (session.status === 'authenticated') return <Navigate to="/" replace />

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      await login(username.trim(), password)
      navigate('/', { replace: true })
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : CONNECTION_ERROR)
      setPassword('')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="mx-auto flex min-h-screen w-full max-w-md flex-col bg-white">
      <section className="bg-cobalt-deep px-6 pb-10 pt-16 text-white">
        <p className="font-display text-4xl font-bold leading-tight">Turnos IA</p>
        <p className="mt-3 text-lg text-mist">Entra para ver los turnos del salón.</p>
      </section>

      <form onSubmit={handleSubmit} noValidate className="flex flex-1 flex-col gap-6 px-6 py-10">
        <div className="flex flex-col gap-2">
          <label htmlFor="username" className="text-lg font-semibold text-ink">
            Usuario
          </label>
          <input
            id="username"
            name="username"
            autoComplete="username"
            autoCapitalize="none"
            spellCheck={false}
            required
            value={username}
            onChange={(event) => setUsername(event.target.value)}
            className="min-h-12 border-2 border-ink px-4 text-lg text-ink focus:border-cobalt"
          />
        </div>

        <div className="flex flex-col gap-2">
          <label htmlFor="password" className="text-lg font-semibold text-ink">
            Contraseña
          </label>
          <input
            id="password"
            name="password"
            type="password"
            autoComplete="current-password"
            required
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            className="min-h-12 border-2 border-ink px-4 text-lg text-ink focus:border-cobalt"
          />
        </div>

        {error && (
          <p role="alert" className="border-l-4 border-red-700 bg-red-50 px-4 py-3 text-lg font-medium text-red-900">
            {error}
          </p>
        )}

        <button
          type="submit"
          disabled={submitting}
          className="mt-auto min-h-12 bg-cobalt px-6 text-lg font-semibold text-white hover:bg-cobalt-deep disabled:bg-neutral-400"
        >
          {submitting ? 'Entrando…' : 'Entrar'}
        </button>
      </form>
    </main>
  )
}
