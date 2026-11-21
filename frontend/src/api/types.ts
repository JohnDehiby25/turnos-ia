// Espejo de los DTOs del backend (backend/src/main/java/com/turnosia/dto).
// Fechas: LocalDate = "YYYY-MM-DD", LocalTime = "HH:mm[:ss]", Instant = ISO-8601 en UTC.

export type AppointmentStatus = 'SCHEDULED' | 'ATTENDED' | 'CANCELLED' | 'NO_SHOW'

export interface UserResponse {
  username: string
  role: string
}

export interface ErrorResponse {
  code: string
  message: string
}

export interface AppointmentResponse {
  id: number
  clientFirstName: string
  clientLastName: string
  clientPhone: string | null
  serviceTypeId: number
  serviceName: string
  start: string
  end: string
  status: AppointmentStatus
  priceCharged: number
  createdAt: string
  updatedAt: string
}

export interface CreateAppointmentRequest {
  firstName: string
  lastName: string
  phone?: string
  serviceTypeId: number
  start: string
  end?: string
  price: number
}

export interface RescheduleAppointmentRequest {
  start: string
  end?: string
}

export interface ChangeStatusRequest {
  status: AppointmentStatus
}

export interface FreeSlotResponse {
  start: string
  end: string
}

export interface DayScheduleResponse {
  date: string
  open: boolean
  openTime: string | null
  closeTime: string | null
  lunchStart: string | null
  lunchEnd: string | null
}

export interface DayScheduleRequest {
  open: boolean
  openTime?: string
  closeTime?: string
  lunchStart?: string
  lunchEnd?: string
  note?: string
}

export interface ServiceResponse {
  id: number
  name: string
  durationMin: number
  price: number | null
}

export interface UpdateServicePriceRequest {
  price: number | null
}

export interface ServiceCount {
  serviceName: string
  count: number
}

export interface StatsSummary {
  from: string
  to: string
  countByStatus: Partial<Record<AppointmentStatus, number>>
  earned: number
  projected: number
  notCollected: number
  tips: number
  topServices: ServiceCount[]
}
