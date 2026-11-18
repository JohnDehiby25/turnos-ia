---
name: precios-y-estadisticas
description: Reglas de precios de servicios y resúmenes de dinero por día, semana y mes. Cargar al tocar precios, price_charged, StatsService o la tool get_income_summary.
---

# Precios y estadísticas

## Reglas
- `service_type.price` es el precio vigente; `appointment.price_charged` es el precio de ESA cita (copia al crearla, ajustable).
  Editar el precio de un servicio nunca cambia citas anteriores.
- Dinero ganado = suma de `price_charged` con estado `ATTENDED`. Proyectado = `SCHEDULED`.
  No percibido = `CANCELLED` + `NO_SHOW` (informativo).
- Todo cálculo en SQL agregado dentro de `StatsService`; `BigDecimal` en Java; `NUMERIC(12,2)` en BD.
- Día/semana/mes en `America/Bogota`; semana = lunes a domingo.
- Solo ADMIN ve estadísticas y edita precios. Los clientes solo ven precios.
- La IA no calcula dinero: usa `list_services` y `get_income_summary` y presenta el resultado.
- Formato de salida: `$ 120.000`.

## Prioridad
Los turnos son el núcleo. No empezar esto antes de cerrar E1-T1..T7.

## Pruebas
Ver "Casos mínimos de precios y estadísticas" en `docs/TESTING.md`.
