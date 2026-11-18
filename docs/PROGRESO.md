# Progreso (el agente lo lee al iniciar y lo actualiza al terminar cada tarea)

## Estado actual
- Etapa: E1 — Backend núcleo (sin IA)
- Última tarea terminada: E1-T2 (commit hecho)
- Tarea en curso: E1-T3 terminada en código y pruebas; pendiente de OK del usuario para commit
- Próxima tarea: E1-T4 (pendiente de confirmar)

## Punto de control
E1-T3 lista en código: `ScheduleService` + `DaySchedule` + 7 tests unitarios en verde
(`ScheduleServiceTest`, sin Docker). Falta: OK para commit + push.
Siguiente: E1-T4 (`AvailabilityService`: huecos por rango y duración).

## Decisiones tomadas
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
