# Progreso (el agente lo lee al iniciar y lo actualiza al terminar cada tarea)

## Estado actual
- Etapa: E1 — cerrada (puerta de salida aprobada por el desarrollador)
- Última tarea publicada: E1-T7, E1-T8 y E1-T9 en commit `b1d72d4` (push a `main`)
- Etapa: E2 — Panel web. E2-T1 en commit `40012d8`. E2-T2 en commit local (`git log`), sin push (pendiente de OK del desarrollador)
- Próxima tarea: E2-T3 (pantalla "Hoy" con turnos reales), con plan previo y OK del desarrollador

## Punto de control
E1-T8 y E1-T9 listas en código y pruebas. Suite completa: 158 tests en verde (Docker encendido).
- Precio: cada cita lleva precio obligatorio (`POST /api/appointments` exige `price`; BD `price_charged NOT NULL` por V4).
- Catálogo: `GET /api/services` y `PUT /api/services/{id}/price` (solo ADMIN).
- Resumen: `GET /api/stats/summary` con `period=DAY|WEEK|MONTH&date=` o `from=&to=` (solo ADMIN). Devuelve conteos por estado, dinero ganado, proyectado, no percibido, propinas y servicios más pedidos.
- Cobertura de `service` (JaCoCo, añadido al `pom.xml` según `TESTING.md`): 98,7 % líneas y 90,3 % ramas (meta ≥ 80 %). Suite: 158 verdes.
- Checklist `SEGURIDAD.md` de E1: completo salvo el usuario de BD con permisos mínimos, que es de E4 (despliegue).
- Pendiente para cerrar E1: (a) demo de API con requests reales (requiere BD arriba y credenciales locales; lo corre el desarrollador); (b) OK del desarrollador para la puerta de salida de E1; (c) commit + push de E1-T7, E1-T8 y E1-T9 con OK explícito. Migración V4 falla si hay citas antiguas sin precio.

## Decisiones tomadas
- 2026-11-21 (E2-T2): sesión en `AuthProvider` + `useSession` (`src/auth/`). Al cargar se llama `GET /api/auth/me`; un 401 significa sin sesión. `RequireSession` redirige a `/login` y `App.tsx` define las rutas (`react-router-dom` 7).
- 2026-11-21 (E2-T2): puertos seguros: Vite escucha solo en `127.0.0.1:5173` con `strictPort`, el proxy `/api` apunta a `127.0.0.1:8080` y `preview` a `127.0.0.1:4173`. El servidor de desarrollo no queda expuesto en la red local.
- 2026-11-21 (E2-T2): diseño "libro de agenda": azul cobalto, negro y blanco; marca en serif (Georgia) y texto en sans. Sin tarjetas genéricas ni etiquetas en mayúsculas. Botones ≥ 48 px, texto base 18 px, foco visible con borde azul.
- 2026-11-21 (E2-T2): cerrar sesión borra la sesión local aunque falle la llamada al servidor, para no dejar a la dueña dentro del panel.
- 2026-11-20 (E1-T8): el precio de cada cita lo decide el admin por caso. `POST /api/appointments` exige `price`, y el servicio rechaza la cita sin precio (`APPOINTMENT_PRICE_REQUIRED`). Migración V4: `price_charged NOT NULL`. El precio del servicio queda como referencia para la UI, no se copia automáticamente desde el API.
- 2026-11-20 (E1-T9): el resumen cuenta por estado y suma dinero en SQL agregado (`AppointmentRepository.totalsByStatus`); `StatsService` solo arma rangos (día, semana lunes-domingo, mes) en `America/Bogota` y el rango libre `from`/`to`. Servicios más pedidos excluyen canceladas. Solo ADMIN.
- 2026-11-20 (E1-T8): solo ADMIN cambia precios (`PUT /api/services/**` en `SecurityConfig`). `GET /api/services` lo ve cualquier sesión y lista solo servicios activos.
- 2026-11-20 (E1-T8): el cambio de precio de un servicio no toca `price_charged` de citas existentes (cubierto por test).
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
- Si la sesión caduca con la app abierta, las llamadas responden 401 pero la app no vuelve a `/login` todavía. Resolver en E2-T3 (manejo de expiración en `AuthProvider`); por eso el ítem de expiración de `SEGURIDAD.md > E2` sigue sin marcar.
- Clientes que pagan a fin de mes (cepillados semanales): posible cuentas por cobrar — fuera de alcance por ahora.

## Bitácora (más reciente arriba)
- 2026-11-21 — E2-T2: login (`LoginPage`), sesión (`AuthProvider`/`useSession`), rutas protegidas (`RequireSession` + `SessionHeader` con "Salir") y pantalla "Hoy" provisional (`TodayPage`, solo fecha y título). Dependencia nueva: `react-router-dom`. Pruebas: 7 de sesión en `App.test.tsx`; frontend en total 12 verdes. `npm run build` y `npm run lint` sin errores. Puertos de Vite fijados a `127.0.0.1`. Sin push (pendiente de OK).
- 2026-11-21 — E2-T1: proyecto `frontend/` con Vite 8 + React 19 + TypeScript + Tailwind 4, `npm test` (Vitest + Testing Library) y `npm run lint` / `npm run build`. Cliente de API tipado en `src/api/` (`types.ts` espeja los DTOs del backend; `client.ts` con `credentials: 'include'`, cabecera `X-XSRF-TOKEN` en escrituras, reintento tras 403 y `ApiError` con `{code, message}`). Vite con proxy `/api` → `localhost:8080`. Pruebas: 5 verdes del cliente. Commit `40012d8`.
- 2026-11-20 — E1-T9: `StatsService` + `StatsController` (`GET /api/stats/summary`), consultas agregadas en `AppointmentRepository`, proyecciones `StatusTotal`/`ServiceCount`, regla ADMIN en `/api/stats/**`. Cambio de E1-T8: el precio pasa a ser obligatorio por cita (V4 `NOT NULL`, validación en API y servicio). Tests: 6 unitarios de `StatsService`, 1 integración con PostgreSQL, 3 de controlador, 3 de seguridad; los fixtures de citas ahora llevan precio. Suite completa 158 verdes. Sin commit (pendiente de OK). Verificación de errores de `AppointmentController.java`: compila sin errores ni avisos; sus tests pasan.
- 2026-11-20 — E1-T8: catálogo de precios (`ServiceCatalogService/Controller`, `ServiceResponse`, `UpdateServicePriceRequest`), precio opcional por cita, `priceCharged` en la respuesta, regla ADMIN en `SecurityConfig`. Tests: 4 de catálogo + 4 de copia de precio en `AppointmentServiceTest` + 4 de controlador + 3 de seguridad. Suite completa 144 verdes. Sin commit (pendiente de OK). Docker estaba apagado: se encendió Docker Desktop para correr las pruebas. Un usuario con rol distinto de ADMIN no se puede guardar en BD (V3 solo permite `ADMIN`), por eso el 403 se prueba con `user(...).roles(...)`.
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
