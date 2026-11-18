# Progreso (el agente lo lee al iniciar y lo actualiza al terminar cada tarea)

## Estado actual
- Etapa: E1 — Backend núcleo (sin IA)
- Última tarea terminada: E1-T6 (commit `912e699`, publicado en GitHub)
- Tarea en curso: E1-T7 (seguridad) terminada en código y pruebas; pendiente de OK del usuario para commit
- Próxima tarea: E1-T8 (precios) — pendiente de confirmar

## Punto de control
E1-T7 lista en código: `SecurityConfig` (sesión con cookie, CSRF con cookie `XSRF-TOKEN`, CORS por `ALLOWED_ORIGINS`,
401/403 en `{code, message}`), `AuthController` (login/logout/me/csrf), `AuthService`, `LoginRateLimiter` (5 intentos en
15 min, bloqueo 15 min, por usuario y por IP), `AdminUserService` + `AdminUserInitializer` (sincroniza `ADMIN_USERNAME` /
`ADMIN_PASSWORD` al arrancar), migración V3 `app_user`. Suite completa: 127 tests en verde (Docker encendido).
Falta: OK para commit + push.
Siguiente: E1-T8 (precios: `ServiceCatalogService`, `GET/PUT /api/services`, `price_charged` al crear cita).

## Decisiones tomadas
- 2026-11-18 (E1-T7): sesión con cookie `httpOnly` en vez de JWT: un solo usuario, el front y la API bajo el mismo dominio y nada de tokens en el navegador. CSRF activo con cookie `XSRF-TOKEN`.
- 2026-11-18 (E1-T7): la contraseña de la dueña se define con `ADMIN_PASSWORD` y se sincroniza al arrancar; si la variable está vacía, la cuenta existente conserva su hash. En la BD solo hay hash BCrypt.
- 2026-11-18 (E1-T7): el límite de intentos cuenta por usuario y por IP, en memoria. Detrás de un proxy (Railway) todas las peticiones vienen de la misma IP, así que el bloqueo es por servidor.
- 2026-11-18 (E1-T7): `GET /api/auth/csrf` expone el token para que el front pueda hacer su primer POST.
- 2026-11-18 (E1-T6): las respuestas de la API devuelven fechas en UTC (`Instant`); las peticiones aceptan fecha con zona horaria (`OffsetDateTime`), así que una hora sin zona se rechaza. El frontend muestra en `America/Bogota`.
- 2026-11-18 (E1-T6): `Appointment.client` y `serviceType` pasan a carga ansiosa porque `open-in-view` está apagado y los DTOs se construyen fuera de la transacción.
- 2026-11-18 (E1-T6): `PUT /api/schedule/days/{date}` guarda el override del día (abrir un domingo o cerrar un día); validación de horas y almuerzo en `ScheduleService`.
- 2026-11-18 (E1-T6): la disponibilidad por servicio resuelve la duración en `AvailabilityService.freeSlotsForService`; los controladores no tocan repositorios.
- 2026-11-18 (E1-T6): la consulta de citas por rango usa `[from, to)`; antes `findAllByStartAtBetween` incluía también el extremo final.
- 2026-11-18 (E1-T5b): la hora de fin es opcional; si no viene, se usa la duración del servicio. La duración real por caso va en la cita (`REQUISITOS.md`).
- 2026-11-18 (E1-T5b): la validación de disponibilidad (horario, almuerzo, solapes) vive en `AvailabilityService.isAvailable`, la misma lógica que muestra los huecos. Si la BD rechaza un solape por una carrera entre dos transacciones, `AppointmentService` lo traduce a `APPOINTMENT_NOT_AVAILABLE`.
- 2026-11-18 (E1-T5b): errores de negocio con código: `APPOINTMENT_IN_PAST`, `APPOINTMENT_START_REQUIRED`, `APPOINTMENT_INVALID_RANGE`, `APPOINTMENT_NOT_AVAILABLE`, `APPOINTMENT_NOT_FOUND`, `APPOINTMENT_NOT_RESCHEDULABLE`, `INVALID_STATUS_TRANSITION`, `SERVICE_NOT_FOUND`.
- 2026-11-18 (E1-T5b): reprogramar solo aplica a citas `SCHEDULED`; reprogramar a un horario que solapa la propia cita original está permitido.
- 2026-11-18 (E1-T5a): nombre y apellido obligatorios; teléfono opcional (vacío = `NULL`). Búsqueda sin distinguir mayúsculas.
- 2026-11-18 (E1-T5a): con una sola coincidencia se usa ese cliente aunque el teléfono dado sea distinto. Si se quiere que un teléfono distinto cree otro cliente, es un cambio de regla.
- 2026-11-18 (E1-T5a): V2 deja `last_name = ''` en clientes existentes (no falla la migración); `ClientService` exige el apellido al crear o resolver un cliente.
- 2026-11-18 (E1-T5a): un teléfono ya registrado a otro cliente se rechaza con `CLIENT_PHONE_TAKEN`, no con una excepción de la BD.
- 2026-11-18 (E1-T5a): nombre y apellido obligatorios; teléfono opcional (vacío = `NULL`). Búsqueda sin distinguir mayúsculas.
- 2026-11-18 (E1-T5a): con una sola coincidencia se usa ese cliente aunque el teléfono dado sea distinto. Si se quiere que un teléfono distinto cree otro cliente, es un cambio de regla.
- 2026-11-18 (E1-T5a): V2 deja `last_name = ''` en clientes existentes (no falla la migración); `ClientService` exige el apellido al crear o resolver un cliente.
- 2026-11-18 (E1-T5a): un teléfono ya registrado a otro cliente se rechaza con `CLIENT_PHONE_TAKEN`, no con una excepción de la BD.
- 2026-11-18 (E1-T4): la disponibilidad bloquea todas las citas no `CANCELLED`, igual que la restricción de BD. El skill `reglas-de-agenda` se alineó en el mismo commit.
- 2026-11-18 (E1-T4): inicios cada 15 min desde la hora de apertura; un hueco debe caber completo antes del cierre y no empezar antes de "ahora".
- 2026-11-18 (E1-T4): la duración llega en minutos. Resolver `serviceId` → duración queda para el controlador (E1-T6).
- 2026-11-18 (E1-T4): `Clock` es un bean `Clock.systemUTC()` (`config/ClockConfig`). La zona de negocio `America/Bogota` vive como constante en `AvailabilityService`.
- 2026-11-18 (E1-T3): `ScheduleService` solo lee el horario de una fecha (sin escribir overrides). Escribir overrides llega con los endpoints (E1-T6) y la pantalla de ajustes (E2-T6).
- 2026-11-18 (E1-T3): un `day_override` reemplaza por completo el horario semanal del día (si no trae almuerzo, ese día no tiene almuerzo). No se mezclan campos.
- 2026-11-18 (E1-T3): domingo sin override = cerrado aunque `working_hours` diga abierto (RF6). Día de semana sin fila en `working_hours` = cerrado.
- 2026-11-18 (E1-T3): sin `Clock` en `ScheduleService`: consulta por `LocalDate`, no depende de la hora actual.
- 2026-11-18 (E1-T1): Spring Boot 4.1.2 en vez de 3.x (la línea 3 quedó EOL en 2026). El resto del stack no cambia.
- 2026-11-18 (E0-T3): ritmo de trabajo 4–5 h por semana (≈ 2 sesiones); fechas objetivo en `ROADMAP.md`.
- 2026-11-18 (E0-T2): una sola agenda (solo la dueña), almuerzo opcional por día, duración/precio por caso, cancelación sin ventana mínima, cobro al terminar, propinas opcionales (`tip`).
- 2026-11-17: PostgreSQL + pgvector en una sola BD
- 2026-11-17: precios y estadísticas son secundarios; se hacen en E1-T8/T9 y E2-T8/T9, después del núcleo de turnos

## Bloqueos
- (ninguno)

## Ideas para después
- Clientes que pagan a fin de mes (cepillados semanales): posible cuentas por cobrar — fuera de alcance por ahora.

## Bitácora (más reciente arriba)
- 2026-11-18 — Problemas del IDE corregidos: avisos de nulos y de Testcontainers; `app.*` tipado en un único `AppProperties` (prefijo `app`) para que el YAML lo reconozca; `ScheduleController` con `/{date}` en el `@RequestMapping` de la clase (aviso informativo). Excepción de negocio con causa y `serialVersionUID`; handlers sin parámetros sin usar. Extensión `ms-ossdata.vscode-pgsql` desinstalada (origen de los 3 errores de SQL y del texto en chino). Análisis tipo IDE: 0 avisos; estricto: 0. Suite 129 verdes; CORS verificado en vivo. Sin commit.
- 2026-11-18 — Corrección: la app no arrancaba porque Spring Boot no leía `.env` (fallaba con "password authentication failed"). Se añadió `spring.config.import: optional:file:../.env[.properties]` en `application.yml`; ahora `./mvnw spring-boot:run` funciona sin exportar variables. Se limpió la cuenta `admin` que había dejado una verificación previa y se definió `ADMIN_USERNAME`/`ADMIN_PASSWORD` en `.env` (valor aleatorio, solo en el archivo local). Verificado: arranque sin variables + login 200 + endpoint protegido 200. Suite 127 verdes.
- 2026-11-18 — E1-T7: sesión con cookie, BCrypt, CORS restringido, CSRF, rate limiting en login, migración V3 `app_user`. 7 de controlador, 5 de integración de seguridad, 8 de límite de intentos, 3 de sincronización de usuaria. Suite completa 127 verdes. Sin commit (pendiente de OK).
- 2026-11-18 — E1-T6 commit `912e699` publicado.
- 2026-11-18 — E1-T6: controladores de citas, disponibilidad y horario; DTOs `record`; `ApiExceptionHandler`. 18 tests de servicio de citas, 16 de controladores (MockMvc), 6 de horario, 2 de disponibilidad, 1 de repositorio. Suite completa 104 verdes. Sin commit (pendiente de OK).
- 2026-11-18 — E1-T5b commit `3ef8660` publicado.
- 2026-11-18 — Auditoría de seguridad de herramientas y descargas: dependencias Maven (136) sin vulnerabilidades conocidas en OSV (control positivo verificado); imagen pgvector con 1 CVE alto sin parche en Debian (pam, EPSS 0,5 %); wrapper de Maven sin verificación de hash → añadido `distributionSha256Sum` (Maven 3.10.1 ya es la última estable); imagen de BD fijada por digest en `docker-compose.yml` y pruebas; Temurin 21.0.8 → 21.0.13 descargado y verificado por SHA-256, pendiente de instalación manual. Suite completa: 77 verdes. Extensión `dof.sql-tools-extenions` desinstalada y reemplazada por `ms-ossdata.vscode-pgsql` v1.34.0 (Microsoft, verificado). Contraseña por defecto retirada de `application.yml`: sin `DB_PASSWORD` el arranque falla con "password authentication failed" (documentado en `backend/README.md`). Pendiente manual: instalar `OpenJDK21U-jdk_x64_windows_hotspot_21.0.13_9.msi` (SHA-256 verificado) desde `%TEMP%\jdk-check`. Sin commit (pendiente de OK).
- 2026-11-18 — E1-T5b: `AppointmentService` (crear, reprogramar, cancelar, marcar atendida/no asistió) + `AvailabilityService.isAvailable` + transiciones en `AppointmentStatus`. 16 unitarias de servicio + 6 de disponibilidad + 6 de integración con PostgreSQL; suite completa 77 verdes. Sin commit (pendiente de OK).
- 2026-11-18 — E1-T5a commit `7ac2515` publicado.
- 2026-11-18 — E1-T5a: cliente con nombre y apellido, teléfono opcional (V2), `ClientService.resolveClient` + `BusinessRuleException`. 10 unitarias de servicio + 2 de repositorio; suite completa 49 verdes. Sin commit (pendiente de OK).
- 2026-11-18 — E1-T4 commit `837256c` publicado (con el skill `reglas-de-agenda` alineado).
- 2026-11-18 — Organización: se borró `database/` de la raíz (solo tenía README). Las migraciones quedan en `backend/src/main/resources/db/migration/`; el README pasó a `backend/README.md`.
- 2026-11-18 — E1-T4: `AvailabilityService` (huecos cada 15 min, citas no canceladas, almuerzo, pasado excluido) + `FreeSlot` + consulta `findNotCancelledOverlapping` + `ClockConfig`. 10 unitarias + 2 de repositorio; suite completa 37 verdes. Diagrama de clases actualizado. Sin commit (pendiente de OK).
- 2026-11-18 — E1-T3: `ScheduleService` + `DaySchedule` (horario efectivo por fecha: override > semanal, domingos cerrados) + 7 tests unitarios verdes. Diagrama de clases actualizado con `DaySchedule`. Sin commit (pendiente de OK).
- 2026-11-18 — E1-T2: entidades JPA (5) + repositorios (5) + tests con Testcontainers; 18 tests verdes. Ajuste en V1: `working_hours` ahora tiene `id` BIGSERIAL (Hibernate no permite @Convert en @Id).
- 2026-11-18 — E1-T1: esqueleto Spring Boot 4.1 (Java 21) + Flyway V1 (esquema + seed Mechas/Cepillado/Detalle) + docker-compose OK + 4 tests verdes (Testcontainers).
- 2026-11-18 — Cierre de sesión: E0 aprobada; plan E1-T1 presentado (pendiente de OK para arrancar).
- 2026-11-18 — E0-T3: fechas objetivo del roadmap llenadas (4–5 h/semana). Etapa E0 completa.
- 2026-11-18 — E0-T2: preguntas abiertas resueltas con la dueña → decisiones en `REQUISITOS.md`; modelo ajustado en `ARQUITECTURA.md` (almuerzo por día, `tip`, duración por caso, agenda única).
- 2026-11-18 — README principal ampliado con el contexto general del proyecto; regla de commit + push siempre.
- 2026-11-18 — E0-T1: estructura del repo (`backend/`, `frontend/`, `database/`, `docs/`), `.gitignore`, `.env.example`; git init + primer commit + push a github.com/JohnDehiby25/turnos-ia.

## Progreso
- Crea un archivo, donde pongas el progreso de los diferentes secciones como back front, pruebas, dockerizacion, empieza por una zona como front, o cualquiera y hasta que no tengas mi aprobacion de todo completo no pasas a la otra, dime que se hizo, corrigió, resumenes, etc
