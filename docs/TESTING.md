# Estrategia de pruebas

## Pirámide
Muchas pruebas unitarias · algunas de integración · pocas end-to-end.

## Backend
- Herramientas: JUnit 5, Mockito, AssertJ, Spring Boot Test (MockMvc), Testcontainers (PostgreSQL + pgvector), JaCoCo.
- **Unitarias (obligatorias):** toda regla en `service` (solapamientos, horarios, domingos, transiciones de estado,
  disponibilidad). Repositorios simulados con Mockito. Sin Spring ni BD: rápidas.
- **Integración:** repositorios y consultas contra PostgreSQL real (Testcontainers); controladores con MockMvc (códigos HTTP,
  validación, seguridad: 401/403).
- **IA:** el LLM SIEMPRE simulado en tests. Probar de forma determinista que cada tool llama al service correcto,
  que exige confirmación, que respeta roles y que el formato de respuesta es el de `BOT_SPEC.md`.
- Cobertura mínima: `service` ≥ 80 % líneas y ramas importantes. La cobertura no reemplaza casos bien pensados.

## Frontend
- Vitest + React Testing Library: componentes clave (lista de turnos, formulario de cita, estados), clientes de API con mocks.
- E2E (Playwright) solo para 2–3 flujos críticos, a partir de E4.

## Convenciones
- Nombre: `metodo_condicion_resultadoEsperado` (ej. `create_overlappingAppointment_throwsConflict`).
- Patrón Given/When/Then (o Arrange/Act/Assert). Un concepto por test.
- Datos de prueba con builders/fábricas pequeñas. Tests independientes, sin orden, sin depender de la hora real
  (inyectar `Clock`).
- Bug encontrado = primero un test que lo reproduce, luego el arreglo.
- Reglas de negocio nuevas: escribir los tests primero cuando sea posible (TDD).

## Casos mínimos de agenda
Solape exacto y parcial · cita que termina justo cuando empieza otra (permitido) · fuera de horario · día cerrado ·
domingo con y sin override · cita en el pasado · transición de estado inválida · cancelar cita ya atendida ·
disponibilidad con varios servicios de distinta duración · cambio de hora de verano no aplica (Colombia) pero probar UTC↔Bogotá.

## Casos mínimos de precios y estadísticas
- El dinero ganado suma solo citas `ATTENDED`; canceladas y no asistidas no suman a "ganado".
- `price_charged` no cambia cuando se edita el precio del servicio.
- Servicio sin precio: no rompe el resumen (se trata como 0 o se marca "por confirmar", según se defina).
- Agrupación por semana lunes–domingo; cita el domingo 11:30 p. m. (Bogotá) cae en el día y la semana correctos aunque en UTC sea lunes.
- Mes con 28/30/31 días; rango sin citas devuelve ceros, no errores.
- `BigDecimal`: sumas exactas sin errores de redondeo.
- `/api/stats/**` y edición de precios devuelven 403 sin rol ADMIN y 401 sin sesión.
