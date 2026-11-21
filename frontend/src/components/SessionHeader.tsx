import { useSession } from '../auth/session'

export function SessionHeader({ username }: { username: string }) {
  const { logout } = useSession()

  return (
    <header className="bg-ink text-white">
      <div className="mx-auto flex max-w-3xl items-center justify-between gap-4 px-5 py-4">
        <div className="min-w-0">
          <p className="font-display text-2xl font-bold">Turnos IA</p>
          <p className="truncate text-base text-mist">Sesión de {username}</p>
        </div>
        <button
          type="button"
          onClick={() => void logout()}
          className="min-h-12 shrink-0 border-2 border-white px-5 text-lg font-semibold hover:bg-white hover:text-ink"
        >
          Salir
        </button>
      </div>
    </header>
  )
}
