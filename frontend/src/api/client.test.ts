import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

type ClientModule = typeof import('./client')

function json(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  })
}

describe('api client', () => {
  let fetchMock: ReturnType<typeof vi.fn>
  let api: ClientModule['api']
  let ApiError: ClientModule['ApiError']

  beforeEach(async () => {
    vi.resetModules()
    ;({ api, ApiError } = await import('./client'))
    fetchMock = vi.fn()
    vi.stubGlobal('fetch', fetchMock)
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('listAppointments_peticionGet_noEnviaCabeceraCsrf', async () => {
    fetchMock.mockResolvedValueOnce(json([]))

    await api.listAppointments({ date: '2026-11-18' })

    const [url, init] = fetchMock.mock.calls[0]
    expect(url).toBe('/api/appointments?date=2026-11-18')
    expect(init.headers['X-XSRF-TOKEN']).toBeUndefined()
  })

  it('createAppointment_primeraEscritura_pideTokenYLoEnvia', async () => {
    fetchMock
      .mockResolvedValueOnce(json({ token: 'abc' }))
      .mockResolvedValueOnce(json({ id: 1 }, 201))

    await api.createAppointment({
      firstName: 'Ana',
      lastName: 'Pérez',
      serviceTypeId: 1,
      start: '2026-11-18T10:00:00-05:00',
      price: 50000,
    })

    expect(fetchMock.mock.calls[0][0]).toBe('/api/auth/csrf')
    const [url, init] = fetchMock.mock.calls[1]
    expect(url).toBe('/api/appointments')
    expect(init.method).toBe('POST')
    expect(init.headers['X-XSRF-TOKEN']).toBe('abc')
  })

  it('createAppointment_respuesta403_renuevaTokenYReintenta', async () => {
    fetchMock
      .mockResolvedValueOnce(json({ token: 'viejo' }))
      .mockResolvedValueOnce(new Response(null, { status: 403 }))
      .mockResolvedValueOnce(json({ token: 'nuevo' }))
      .mockResolvedValueOnce(json({ id: 2 }, 201))

    const created = await api.createAppointment({
      firstName: 'Ana',
      lastName: 'Pérez',
      serviceTypeId: 1,
      start: '2026-11-18T10:00:00-05:00',
      price: 50000,
    })

    expect(created).toEqual({ id: 2 })
    expect(fetchMock.mock.calls[3][1].headers['X-XSRF-TOKEN']).toBe('nuevo')
  })

  it('login_credencialesIncorrectas_lanzaApiErrorConCodigo', async () => {
    fetchMock
      .mockResolvedValueOnce(json({ token: 'abc' }))
      .mockResolvedValueOnce(json({ code: 'INVALID_CREDENTIALS', message: 'Usuario o clave incorrectos' }, 401))

    const error = await api.login('admin', 'mala').catch((e: unknown) => e)

    expect(error).toBeInstanceOf(ApiError)
    expect(error).toMatchObject({ status: 401, code: 'INVALID_CREDENTIALS', message: 'Usuario o clave incorrectos' })
  })

  it('getDaySchedule_errorSinCuerpoJson_lanzaApiErrorDesconocido', async () => {
    fetchMock.mockResolvedValueOnce(new Response('<html>', { status: 500, statusText: 'Server Error' }))

    const error = await api.getDaySchedule('2026-11-22').catch((e: unknown) => e)

    expect(error).toMatchObject({ status: 500, code: 'UNKNOWN' })
  })
})
