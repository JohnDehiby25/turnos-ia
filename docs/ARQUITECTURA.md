# Arquitectura

```
[Cliente WhatsApp] ──► Webhook ─┐
[Panel web PWA (React/TS/Tailwind)] ──► API REST ──► Spring Boot (capas) ──► PostgreSQL + pgvector
                                                    └─► LLM / Embeddings (proveedor externo)
```

## Capas del backend (`com.turnosia`)
```
controller/    AppointmentController, ScheduleController, ChatController, WhatsappWebhookController
service/       AppointmentService, AvailabilityService, ScheduleService, KnowledgeService
repository/    AppointmentRepository, ClientRepository, ServiceTypeRepository, ...
domain/        Appointment, Client, ServiceType, WorkingHours, DayOverride, KnowledgeChunk, AppointmentStatus
dto/           records de request/response
ai/            AssistantService, tools/ (una clase por herramienta), prompts
integration/whatsapp/   cliente de envío + verificación de firma del webhook
config/        seguridad, CORS, Spring AI
```
Regla: `controller` y `ai.tools` llaman a `service`; solo `service` llama a `repository`.

## Modelo de datos (PostgreSQL)
- `client(id, first_name, last_name, phone UNIQUE NULL, created_at)` — nombre y apellido obligatorios; teléfono opcional. Se busca por nombre y apellido (sin distinguir mayúsculas); si hay homónimos, se pide el teléfono.
- `service_type(id, name, duration_min, price NUMERIC(12,2) NULL, active)` — `price` NULL = "precio por confirmar"; `duration_min` y `price` son estimados: la cita define los valores reales
- `appointment(id, client_id, service_type_id, start_at, end_at, status, price_charged NUMERIC(12,2), tip NUMERIC(12,2) NULL, notes, created_at, updated_at)`
  - `start_at`/`end_at` = duración real de la cita (por caso, según largo/estado del cabello). `tip` = propina opcional.
  - `created_at` = momento en que se asignó la cita. Índice por `start_at`. Restricción anti-solapamiento (una sola persona atiende: no hay citas paralelas).
- `working_hours(weekday, is_open, open_time, close_time, lunch_start NULL, lunch_end NULL)` — horario base semanal; almuerzo opcional por día (días muy agendados: sin almuerzo)
- `day_override(date, is_open, open_time NULL, close_time NULL, lunch_start NULL, lunch_end NULL, note)` — excepciones (ej. domingos que sí se trabajan, días sin almuerzo); `lunch NULL` = sin almuerzo ese día
- `knowledge_chunk(id, source, content, embedding vector(N), created_at)` — SOLO para búsqueda semántica

## ¿Para qué sirve la base vectorial aquí?
Sí: responder preguntas abiertas ("¿cuánto dura un alisado?", "¿qué cuidados después de las mechas?"),
entender servicios descritos libremente por el cliente, notas de preferencias.
No: guardar citas ni calcular disponibilidad (eso es SQL exacto).

## API REST (v1)
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/appointments?date=2026-11-18` | Turnos de un día |
| GET | `/api/appointments?from=&to=&status=` | Turnos por rango/estado |
| GET | `/api/availability?from=&to=&serviceId=` | Espacios libres |
| POST | `/api/appointments` | Crear cita |
| PATCH | `/api/appointments/{id}/status` | Cambiar estado |
| PUT | `/api/appointments/{id}` | Reprogramar |
| GET/PUT | `/api/schedule/days/{date}` | Abrir/cerrar un día (domingos) |
| GET | `/api/services` | Lista de servicios con precios |
| PUT | `/api/services/{id}` | Editar precio/duración (solo ADMIN) |
| GET | `/api/stats/summary?from=&to=&groupBy=day\|week\|month` | Resumen de citas y dinero (solo ADMIN) |
| POST | `/api/chat` | Mensaje al bot (chat web) |
| GET/POST | `/webhooks/whatsapp` | Verificación y mensajes entrantes |

Respuestas de error uniformes: `{ "code": "...", "message": "..." }`.

## Frontend
`frontend/src`: `pages/` (Hoy, Agenda, Disponibilidad, Chat, Ajustes), `components/`, `api/` (cliente tipado),
`types/`. PWA instalable. Sin lógica de negocio en el front: solo presenta lo que devuelve la API.

## Despliegue
`docker-compose.yml` con servicios `db` (postgres+pgvector), `backend`, `frontend` (nginx).
CI en GitHub Actions: build + tests en cada PR. Deploy a Railway desde `main`. Dominio apuntando al front;
la API bajo `/api`. Variables de entorno: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `LLM_API_KEY`,
`WHATSAPP_TOKEN`, `WHATSAPP_VERIFY_TOKEN`, `JWT_SECRET`.

## Etapa mobile (E6, al final)
Se decide después de tener web + nube funcionando. Opciones:
- **Capacitor sobre la web (recomendada por defecto):** reutiliza el 100 % del front React; empaqueta como app y agrega notificaciones.
- **React Native / Expo:** UI nativa, reutiliza TypeScript, tipos y cliente de API, pero hay que rehacer las pantallas.
La API no cambia en ninguno de los dos casos; por eso la web y el backend se construyen primero.

## Precios y estadísticas
- `ServiceCatalogService`: lista y edita servicios/precios. `StatsService`: resúmenes. Ambos en la capa `service`.
- Las estadísticas son consultas agregadas en SQL (`GROUP BY` sobre `start_at AT TIME ZONE 'America/Bogota'`), no cálculos en Java ni en la IA.
- Semana = lunes a domingo. Una cita cuenta en el día/semana/mes de su `start_at` en hora de Bogotá.
- Dinero con `BigDecimal` y `NUMERIC` en BD; formateo `$ 120.000` solo en la presentación.
- Respuesta de `/api/stats/summary`: por periodo → `appointmentsByStatus`, `incomeEarned`, `incomeProjected`, `incomeLost`, `topServices`, `tipsTotal` (suma de `tip` de citas `ATTENDED`, aparte del dinero de servicios).
- La IA no tiene lógica de estadísticas: la tool `get_income_summary` llama a `StatsService`.
- No se usa la BD vectorial para nada de esto.
