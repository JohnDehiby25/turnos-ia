# Turnos IA

Asistente de turnos con IA para un salón de belleza pequeño.

Gestiona las citas del salón y las atiende por **WhatsApp** o **chat web** mediante un
bot de IA, con un **panel web (PWA)** para que la dueña vea y gestione el día de forma
simple: letra grande, pocos botones, máximo 3 acciones por pantalla.

## Qué hace
- **Turnos**: ver el día, crear, cancelar, reprogramar y marcar atendida / no asistió,
  sin dobles reservas (validación de solapamiento en transacción).
- **Horario**: semanal configurable + excepciones por día (por defecto, domingos cerrados).
- **Servicios y precios**: catálogo editable; cada cita guarda su precio cobrado
  (`price_charged`), así cambiar un precio no altera citas anteriores.
- **Resumen**: citas por estado y dinero ganado / proyectado / no percibido, por día, semana y mes.
- **Bot de IA**: responde en lenguaje natural usando *tool calling*; nunca inventa horarios
  ni calcula dinero (siempre consulta el backend). Búsqueda semántica con `pgvector`.

## Stack
- **Backend**: Java 21, Spring Boot 3, Maven, Spring Web / Data JPA / Security / AI, Flyway.
- **Base de datos**: PostgreSQL 16 + `pgvector` (SQL y vectores en una sola BD).
- **Frontend**: React + TypeScript + Vite + Tailwind CSS (mobile-first, PWA).
- **Infra**: Docker + docker-compose, CI en GitHub Actions, deploy en Railway.

## Estado
En desarrollo por etapas. Ver `docs/ROADMAP.md` (plan) y `docs/PROGRESO.md` (avance).

## Estructura
```
backend/    frontend/    database/    docs/    .opencode/
```

## Documentación
- `AGENTS.md` — protocolo de trabajo por etapas y reglas del proyecto.
- `docs/` — requisitos, arquitectura, roadmap, seguridad, testing y más.

## Licencia
MIT. Ver `LICENSE`.
