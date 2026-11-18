# Roadmap y planning por etapas

Reglas: una tarea a la vez · cada etapa termina en una **puerta de salida** que yo apruebo · web primero, mobile al final.
Estimación en "sesiones" (1 sesión ≈ 2 h). Las fechas se llenan en E0-T3 según mis horas libres por semana.

| Etapa | Objetivo | Sesiones aprox. | Fecha objetivo |
|---|---|---|---|
| E0 | Preparación | 1 | ____ |
| E1 | Backend núcleo (sin IA) + precios y resumen | 7–8 | ____ |
| E2 | Panel web (turnos, precios, resumen) | 6–8 | ____ |
| E3 | IA: chat web + vectorial | 4–5 | ____ |
| E4 | Nube: Docker, dominio, CI | 2–3 | ____ |
| E5 | WhatsApp | 3–4 | ____ |
| E6 | Mobile | 4–6 | ____ |

## E0 — Preparación
- [ ] E0-T1 Repo GitHub, estructura, `.gitignore`, `.env.example`, ramas (`main` + `feature/*`)
- [ ] E0-T2 Resolver preguntas abiertas de `REQUISITOS.md` con la dueña
- [ ] E0-T3 Definir mis horas por semana y llenar fechas objetivo de esta tabla
**Puerta:** repo limpio en GitHub + preguntas abiertas respondidas.

## E1 — Backend núcleo (sin IA)
- [ ] E1-T1 Proyecto Spring Boot + PostgreSQL local (docker) + Flyway V1 (esquema + servicios iniciales: Mechas, Cepillado, Detalle). Usar el `docker-compose.yml` incluido
- [ ] E1-T2 Entidades + repositorios + test de repositorio (Testcontainers)
- [ ] E1-T3 `ScheduleService` (horario semanal, overrides, domingos cerrados) + tests
- [ ] E1-T4 `AvailabilityService` (huecos por rango y duración) + tests
- [ ] E1-T5 `AppointmentService` (crear, cancelar, reprogramar, estados, sin solapamientos) + tests
- [ ] E1-T6 Controladores REST + DTOs + manejo global de errores + tests MockMvc
- [ ] E1-T7 Seguridad: login de la dueña (BCrypt + JWT o sesión), CORS, validaciones
- [ ] E1-T8 Precios: `ServiceCatalogService`, `GET/PUT /api/services`, `price_charged` al crear cita + tests (solo después de E1-T1..T7)
- [ ] E1-T9 Estadísticas: `StatsService` + `GET /api/stats/summary` (día/semana/mes, dinero ganado/proyectado/no percibido) + tests
**Puerta:** `./mvnw test` verde, cobertura de `service` ≥ 80 %, API probada con requests de ejemplo,
checklist de `SEGURIDAD.md` (etapa E1) completo. Demo: pedir turnos del 18/11/2026 por API, ver precios y pedir el resumen de la semana.

## E2 — Panel web
- [ ] E2-T1 Proyecto React + TS + Tailwind + cliente de API tipado
- [ ] E2-T2 Login + rutas protegidas
- [ ] E2-T3 Pantalla "Hoy" (turnos con cliente, hora, tipo, estado, fecha de asignación)
- [ ] E2-T4 Crear / cancelar / marcar atendida
- [ ] E2-T5 Disponibilidad por rango de fechas
- [ ] E2-T6 Ajustes: horario y apertura de domingos
- [ ] E2-T7 PWA instalable + revisión de accesibilidad en 360px
- [ ] E2-T8 Pantalla "Precios" (ver y editar)
- [ ] E2-T9 Pantalla "Resumen": día / semana / mes con citas por estado y dinero
**Puerta:** mi mamá puede ver y gestionar el día sin ayuda; tests de componentes clave en verde; checklist E2.

## E3 — IA
- [ ] E3-T1 Capa `ai` con proveedor LLM intercambiable (interfaz + adaptador)
- [ ] E3-T2 Tools sobre los `service` (listar turnos, disponibilidad, crear, cancelar, estado, abrir día, ver precios, resumen de dinero)
- [ ] E3-T3 `/api/chat` + confirmación antes de escribir + tests con LLM simulado (mock)
- [ ] E3-T4 pgvector + `knowledge_chunk` + carga de FAQ + `search_knowledge`
- [ ] E3-T5 Pantalla de chat en el panel
- [ ] E3-T6 Defensas contra prompt injection y límites por rol
**Puerta:** el bot responde "dame los turnos del 18 de noviembre de 2026" con el formato de `BOT_SPEC.md`, y también responde precios y "¿cuánto gané esta semana?"; checklist E3.

## E4 — Nube
- [ ] E4-T1 Dockerfiles + `docker-compose.yml`
- [ ] E4-T2 GitHub Actions (build + tests en cada PR) + Dependabot
- [ ] E4-T3 Deploy en Railway, dominio, HTTPS, variables de entorno, backups
**Puerta:** sistema accesible por el dominio; checklist E4.

## E5 — WhatsApp
- [ ] E5-T1 Webhook + verificación de firma
- [ ] E5-T2 Flujo dueña (consultas) y flujo cliente (reservar/cancelar lo suyo)
- [ ] E5-T3 Pregunta semanal del domingo
**Puerta:** conversación real de prueba con ambos flujos; checklist E5.

## E6 — Mobile
- [ ] E6-T1 Decidir enfoque (Capacitor sobre la web vs. React Native/Expo) con pros y contras actualizados
- [ ] E6-T2 Prototipo mínimo ("Hoy" + notificaciones)
- [ ] E6-T3 Pruebas en dispositivo y publicación/instalación
**Puerta:** app instalada en el celular de mi mamá.
