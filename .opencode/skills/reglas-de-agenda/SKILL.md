---
name: reglas-de-agenda
description: Reglas de negocio de turnos del salón (disponibilidad, solapamientos, estados, domingos). Cargar al tocar citas, horarios o disponibilidad.
---

# Reglas de agenda

## Cuándo usar
Al crear o modificar lógica de citas, disponibilidad, horario o estados.

## Reglas
- Una cita ocupa `[start_at, end_at)`, con `end_at = start_at + duración del servicio`.
- Dos citas no pueden solaparse (validar dentro de transacción; además restricción en BD).
- Solo se agenda dentro del horario del día: `day_override` tiene prioridad sobre `working_hours`.
- Domingos: cerrados salvo `day_override.is_open = true`.
- No se agenda en el pasado.
- Transiciones de estado válidas: `SCHEDULED → ATTENDED | CANCELLED | NO_SHOW`. Los estados finales no vuelven atrás
  (si hay error humano, crear corrección auditada).
- `created_at` nunca se modifica: es la fecha en que se asignó la cita.
- Zona horaria `America/Bogota`; guardar UTC.

## Disponibilidad (algoritmo)
1. Tomar horario del día (override o base) y restar el almuerzo. 2. Restar citas no `CANCELLED` (las mismas que bloquea la restricción de BD). 3. Devolver huecos ≥ duración del servicio,
en pasos de 15 min. 4. Para rangos, repetir por día.

## Tests mínimos
Solapamiento exacto y parcial, borde (termina cuando otra empieza = permitido), día cerrado, domingo con override,
transición inválida.
