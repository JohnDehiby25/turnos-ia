# Buenas prácticas

## Filosofía
Simple y claro antes que "elegante". KISS, YAGNI, DRY con criterio. Una persona (yo) debe poder mantenerlo.

## Diagramas
Antes de hacer cualquier tipo de codigo, quiero que diseñes el diagrama de clases y diagrama de bases de datos y cualquier diagrama uml necesario para entender las relaciones y funcionalidades, crea una carpeta diseño donde añadas eso siempre

## Patrones de diseño: solo cuando resuelven un problema real
| Patrón | Dónde sí aplica en este proyecto | Dónde NO |
|---|---|---|
| Repository | Acceso a datos (Spring Data) | — |
| Service Layer | Reglas de negocio | Meter lógica en controllers |
| DTO + Mapper | Separar API de entidades | Exponer entidades JPA |
| Strategy | Reglas de disponibilidad si crecen (por servicio, por estilista) | Si hay una sola regla |
| State (simple, con enum) | Transiciones válidas de `AppointmentStatus` | Una jerarquía de clases para 4 estados |
| Adapter / Port | Proveedor de LLM y cliente de WhatsApp intercambiables | Envolver todo "por si acaso" |
| Factory / Builder | Builders de datos en tests | Producción, salvo objetos complejos |
| Observer / eventos | Notificar al crear/cancelar cita (solo si E5 lo necesita) | Antes de tener un consumidor |
Regla: al usar un patrón, dime cuál, qué problema resuelve y qué costo agrega.

## Estructuras de datos: solo las necesarias
- Usar las estándar: `List`, `Map`, `Set`, `TreeMap` (citas ordenadas por hora), `Optional`.
- Caso real en este proyecto: calcular huecos libres = recorrer citas ordenadas por `start_at` y restar intervalos
  (lista ordenada + un recorrido). No hace falta árbol de intervalos ni estructura propia con una agenda diaria.
- Crear una estructura propia solo si hay un problema medido o evidente que las estándar no resuelven; justificarlo en una frase.

## Backend
- Inyección por constructor, clases pequeñas con una responsabilidad, `record` para DTOs, `Optional` en vez de null.
- Excepciones de dominio claras; manejo central con `@RestControllerAdvice`.
- Tiempo vía `Clock` inyectable (testeable). Dinero con `BigDecimal`.
- Logs útiles con niveles; sin datos personales ni secretos.
- Versionar la API (`/api/v1`) cuando algo se rompa.

## Frontend
- Componentes pequeños, tipos estrictos, estado local salvo que haga falta global, nada de lógica de negocio.
- Estados de carga / vacío / error siempre.

## Git
- Ramas `feature/<tarea>` desde `main`, Conventional Commits, PRs pequeños, CI en verde antes de fusionar.
- Un commit por tarea aprobada (`feat(E1-T3): schedule service`).

## Documentación
README claro, `docs/` actualizada, decisiones relevantes en `PROGRESO.md > Decisiones`.
