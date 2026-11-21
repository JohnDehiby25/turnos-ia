import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { api, ApiError } from './api/client'
import { AuthProvider } from './auth/AuthProvider'
import { AppRoutes } from './App'

vi.mock('./api/client', async () => {
  const actual = await vi.importActual<typeof import('./api/client')>('./api/client')
  return {
    ...actual,
    api: { me: vi.fn(), login: vi.fn(), logout: vi.fn() },
  }
})

const usuaria = { username: 'admin', role: 'ADMIN' }

function renderApp(path: string) {
  return render(
    <AuthProvider>
      <MemoryRouter initialEntries={[path]}>
        <AppRoutes />
      </MemoryRouter>
    </AuthProvider>,
  )
}

describe('panel de sesión', () => {
  beforeEach(() => {
    vi.mocked(api.me).mockRejectedValue(new ApiError(401, 'UNAUTHENTICATED', 'Necesitas iniciar sesión'))
  })

  afterEach(() => {
    cleanup()
    vi.clearAllMocks()
  })

  it('sinSesion_rutaProtegida_muestraLogin', async () => {
    renderApp('/')

    expect(await screen.findByRole('button', { name: 'Entrar' })).toBeTruthy()
    expect(screen.queryByRole('heading', { name: 'Hoy' })).toBeNull()
  })

  it('login_credencialesIncorrectas_muestraErrorYNoEntra', async () => {
    vi.mocked(api.login).mockRejectedValue(
      new ApiError(401, 'INVALID_CREDENTIALS', 'Usuario o clave incorrectos'),
    )
    renderApp('/login')

    fireEvent.change(await screen.findByLabelText('Usuario'), { target: { value: 'admin' } })
    fireEvent.change(screen.getByLabelText('Contraseña'), { target: { value: 'mala' } })
    fireEvent.click(screen.getByRole('button', { name: 'Entrar' }))

    expect((await screen.findByRole('alert')).textContent).toBe('Usuario o clave incorrectos')
    expect(screen.queryByRole('heading', { name: 'Hoy' })).toBeNull()
  })

  it('login_sinConexion_muestraMensajeDeRed', async () => {
    vi.mocked(api.login).mockRejectedValue(new TypeError('Failed to fetch'))
    renderApp('/login')

    fireEvent.change(await screen.findByLabelText('Usuario'), { target: { value: 'admin' } })
    fireEvent.click(screen.getByRole('button', { name: 'Entrar' }))

    expect((await screen.findByRole('alert')).textContent).toContain('No hay conexión con el servidor')
  })

  it('login_credencialesCorrectas_muestraHoy', async () => {
    vi.mocked(api.login).mockResolvedValue(usuaria)
    renderApp('/login')

    fireEvent.change(await screen.findByLabelText('Usuario'), { target: { value: ' admin ' } })
    fireEvent.change(screen.getByLabelText('Contraseña'), { target: { value: 'correcta' } })
    fireEvent.click(screen.getByRole('button', { name: 'Entrar' }))

    expect(await screen.findByRole('heading', { name: 'Hoy' })).toBeTruthy()
    expect(api.login).toHaveBeenCalledWith('admin', 'correcta')
  })

  it('sesionActiva_login_redirigeAHoy', async () => {
    vi.mocked(api.me).mockResolvedValue(usuaria)
    renderApp('/login')

    expect(await screen.findByRole('heading', { name: 'Hoy' })).toBeTruthy()
  })

  it('logout_sesionActiva_volveALogin', async () => {
    vi.mocked(api.me).mockResolvedValue(usuaria)
    vi.mocked(api.logout).mockResolvedValue(undefined)
    renderApp('/')

    fireEvent.click(await screen.findByRole('button', { name: 'Salir' }))

    expect(await screen.findByRole('button', { name: 'Entrar' })).toBeTruthy()
    expect(api.logout).toHaveBeenCalledTimes(1)
  })

  it('logout_errorDelServidor_igualCierraSesionLocal', async () => {
    vi.mocked(api.me).mockResolvedValue(usuaria)
    vi.mocked(api.logout).mockRejectedValue(new TypeError('Failed to fetch'))
    renderApp('/')

    fireEvent.click(await screen.findByRole('button', { name: 'Salir' }))

    expect(await screen.findByRole('button', { name: 'Entrar' })).toBeTruthy()
  })
})
