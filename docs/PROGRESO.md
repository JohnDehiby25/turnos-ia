# Progreso (el agente lo lee al iniciar y lo actualiza al terminar cada tarea)

## Estado actual
- Etapa: E1 — Backend núcleo (sin IA)
- Última tarea terminada: E0-T3 (puerta de E0 aprobada)
- Tarea en curso: E1-T1 (plan presentado, pendiente de aprobación)
- Próxima tarea: E1-T1

## Punto de control (cierre de sesión 2026-11-18)
Estoy en: E1-T1 con el plan ya presentado (diagramas en `diseño/` → esqueleto Spring Boot 3 → Flyway V1
con seed de servicios → BD con docker-compose → test Testcontainers). Pendiente de aprobación del plan.
Falta: aprobar el plan, iniciar Docker Desktop y ejecutar E1-T1.
Siguiente paso: aprobar el plan y arrancar E1-T1 (rama `feature/E1-T1`).

## Decisiones tomadas
- 2026-11-18 (E0-T3): ritmo de trabajo 4–5 h por semana (≈ 2 sesiones); fechas objetivo en `ROADMAP.md`.
- 2026-11-18 (E0-T2): una sola agenda (solo la dueña), almuerzo opcional por día, duración/precio por caso, cancelación sin ventana mínima, cobro al terminar, propinas opcionales (`tip`).
- 2026-11-17: PostgreSQL + pgvector en una sola BD
- 2026-11-17: precios y estadísticas son secundarios; se hacen en E1-T8/T9 y E2-T8/T9, después del núcleo de turnos

## Bloqueos
- (ninguno)

## Ideas para después
- Clientes que pagan a fin de mes (cepillados semanales): posible cuentas por cobrar — fuera de alcance por ahora.

## Bitácora (más reciente arriba)
- 2026-11-18 — Cierre de sesión: E0 aprobada; plan E1-T1 presentado (pendiente de OK para arrancar).
- 2026-11-18 — E0-T3: fechas objetivo del roadmap llenadas (4–5 h/semana). Etapa E0 completa.
- 2026-11-18 — E0-T2: preguntas abiertas resueltas con la dueña → decisiones en `REQUISITOS.md`; modelo ajustado en `ARQUITECTURA.md` (almuerzo por día, `tip`, duración por caso, agenda única).
- 2026-11-18 — README principal ampliado con el contexto general del proyecto; regla de commit + push siempre.
- 2026-11-18 — E0-T1: estructura del repo (`backend/`, `frontend/`, `database/`, `docs/`), `.gitignore`, `.env.example`; git init + primer commit + push a github.com/JohnDehiby25/turnos-ia.

## Progreso
- Crea un archivo, donde pongas el progreso de los diferentes secciones como back front, pruebas, dockerizacion, empieza por una zona como front, o cualquiera y hasta que no tengas mi aprobacion de todo completo no pasas a la otra, dime que se hizo, corrigió, resumenes, etc
