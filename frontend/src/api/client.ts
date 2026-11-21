import type {
  AppointmentResponse,
  AppointmentStatus,
  ChangeStatusRequest,
  CreateAppointmentRequest,
  DayScheduleRequest,
  DayScheduleResponse,
  ErrorResponse,
  FreeSlotResponse,
  RescheduleAppointmentRequest,
  ServiceResponse,
  StatsSummary,
  UpdateServicePriceRequest,
  UserResponse,
} from './types'

export class ApiError extends Error {
  readonly status: number
  readonly code: string

  constructor(status: number, code: string, message: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.code = code
  }
}

const UNSAFE_METHODS = new Set(['POST', 'PUT', 'PATCH', 'DELETE'])
const CSRF_HEADER = 'X-XSRF-TOKEN'

let csrfToken: string | null = null

async function fetchCsrfToken(): Promise<string> {
  const response = await fetch('/api/auth/csrf', { credentials: 'include' })
  const body = (await response.json()) as { token: string }
  csrfToken = body.token
  return body.token
}

async function send(method: string, path: string, body?: unknown): Promise<Response> {
  const headers: Record<string, string> = {}
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  if (UNSAFE_METHODS.has(method)) {
    headers[CSRF_HEADER] = csrfToken ?? (await fetchCsrfToken())
  }
  return fetch(path, {
    method,
    credentials: 'include',
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  })
}

async function request<T>(method: string, path: string, body?: unknown): Promise<T> {
  let response = await send(method, path, body)
  if (response.status === 403 && UNSAFE_METHODS.has(method)) {
    await fetchCsrfToken()
    response = await send(method, path, body)
  }
  if (!response.ok) {
    const error = (await response.json().catch(() => null)) as ErrorResponse | null
    throw new ApiError(response.status, error?.code ?? 'UNKNOWN', error?.message ?? response.statusText)
  }
  if (response.status === 204) return undefined as T
  return (await response.json()) as T
}

export interface AppointmentFilter {
  date?: string
  from?: string
  to?: string
  status?: AppointmentStatus
}

export interface StatsQuery {
  period?: 'DAY' | 'WEEK' | 'MONTH'
  date?: string
  from?: string
  to?: string
}

function query(params: object): string {
  const search = new URLSearchParams()
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined) search.set(key, String(value))
  }
  const text = search.toString()
  return text ? `?${text}` : ''
}

export const api = {
  login: (username: string, password: string) =>
    request<UserResponse>('POST', '/api/auth/login', { username, password }),
  logout: () => request<void>('POST', '/api/auth/logout'),
  me: () => request<UserResponse>('GET', '/api/auth/me'),

  listAppointments: (filter: AppointmentFilter) =>
    request<AppointmentResponse[]>('GET', `/api/appointments${query(filter)}`),
  createAppointment: (body: CreateAppointmentRequest) =>
    request<AppointmentResponse>('POST', '/api/appointments', body),
  rescheduleAppointment: (id: number, body: RescheduleAppointmentRequest) =>
    request<AppointmentResponse>('PUT', `/api/appointments/${id}`, body),
  changeAppointmentStatus: (id: number, body: ChangeStatusRequest) =>
    request<AppointmentResponse>('PATCH', `/api/appointments/${id}/status`, body),

  freeSlots: (from: string, to: string, serviceId: number) =>
    request<FreeSlotResponse[]>('GET', `/api/availability${query({ from, to, serviceId })}`),

  getDaySchedule: (date: string) =>
    request<DayScheduleResponse>('GET', `/api/schedule/days/${date}`),
  saveDaySchedule: (date: string, body: DayScheduleRequest) =>
    request<DayScheduleResponse>('PUT', `/api/schedule/days/${date}`, body),

  listServices: () => request<ServiceResponse[]>('GET', '/api/services'),
  updateServicePrice: (id: number, body: UpdateServicePriceRequest) =>
    request<ServiceResponse>('PUT', `/api/services/${id}/price`, body),

  statsSummary: (params: StatsQuery) =>
    request<StatsSummary>('GET', `/api/stats/summary${query(params)}`),
}
