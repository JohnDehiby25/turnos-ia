# Turnos IA — Asistente de turnos para salón de belleza

Sistema para gestionar los turnos de un salón de belleza pequeño. La dueña (mi mamá) NO es técnica:
todo debe ser simple, con letra grande y pocos botones. Un bot de IA atiende por WhatsApp y/o chat web,
y un panel web muestra las citas con claridad.

## PROTOCOLO DE TRABAJO POR ETAPAS (lo más importante)
Yo (el desarrollador) quiero ir paso a paso. NO construyas el sistema de una sola vez.

**Al iniciar cada sesión:**
1. Lee `docs/PROGRESO.md` y `docs/ROADMAP.md`.
2. Resúmeme en máx. 5 líneas: etapa actual, qué quedó hecho, qué quedó a medias y cuál es la próxima tarea.
3. Pregúntame UNA cosa: "¿Seguimos con <próxima tarea> o prefieres otra cosa?". Espera mi respuesta antes de tocar código.

**Durante la sesión:**
- Trabaja UNA tarea del roadmap a la vez (tamaño ≈ 1 sesión corta). Si es más grande, propón dividirla.
- No empieces la siguiente etapa sin mi aprobación explícita (cada etapa tiene una "puerta de salida" en el roadmap).
- No agregues funcionalidades fuera de la tarea actual. Si ves algo útil, anótalo en `docs/PROGRESO.md > Ideas`.
- Antes de escribir código en una tarea nueva, dime el plan en 3–5 viñetas y espera mi OK.

**Al terminar cada tarea, muestra un REPORTE DE AVANCE:**
1. Qué hiciste (2–4 líneas).
2. Cómo verlo funcionando (comando, URL o ejemplo de request/respuesta).
3. Pruebas: cuáles escribiste y resultado (`X passed`).
4. Seguridad y buenas prácticas aplicadas (1–3 viñetas).
5. Próxima tarea sugerida.
Luego actualiza `docs/PROGRESO.md` y haz commit + push a GitHub (regla fija, ver "Reglas críticas").

**Al cerrar la sesión** (si digo "cerramos" o similar): deja `docs/PROGRESO.md` con un punto de control claro
("Estoy en…, falta…, siguiente paso…").

## Idioma
- Documentación, textos de la interfaz y respuestas del bot: español (Colombia).
- Código (clases, variables, endpoints, commits): inglés.
- Zona horaria SIEMPRE `America/Bogota`. Guardar fechas en UTC en BD, mostrar en hora local.

## Stack (no cambiar sin preguntarme)
- Backend: Java 21, Spring Boot 4, Maven, Spring Web, Spring Data JPA, Validation, Spring Security, Flyway, Spring AI.
- Base de datos: PostgreSQL 16 + extensión pgvector (SQL y vectores en UNA sola BD).
- Frontend web: React + TypeScript + Vite + Tailwind CSS (mobile-first, PWA).
- IA: LLM con *tool calling* + embeddings (proveedor configurable por variables de entorno).
- Canal: WhatsApp Cloud API (webhook) y chat web.
- Infra: Docker + docker-compose, GitHub (+ Actions), despliegue en Railway, dominio propio.
- App mobile: etapa FINAL. Solo se decide el enfoque (Capacitor sobre la web vs. React Native/Expo) al llegar ahí.

## Arquitectura: cliente-servidor en capas
Front → API REST → Backend en capas → PostgreSQL/pgvector.
Capas (dependencias solo hacia abajo): `controller → service → repository → domain`.
- `controller`: HTTP, validación de entrada, DTOs. Sin lógica de negocio.
- `service`: reglas de negocio y transacciones.
- `repository`: acceso a datos. Sin lógica de negocio.
- `domain`: entidades y enums.
- `ai`: orquestación del bot; sus tools llaman a `service`, nunca a `repository`.
- `integration.whatsapp`: webhook y cliente de envío.
Detalle en `docs/ARQUITECTURA.md`.

## Estructura del repo
```
backend/  frontend/  database/  diseño/  docs/  .opencode/{skills,command}/  docker-compose.yml
```

## Documentos: cuándo leerlos
- Siempre: `PROGRESO.md`, `ROADMAP.md`, `REQUISITOS.md`, `ARQUITECTURA.md` (cargados por opencode.json).
- Al tocar código de backend o frontend: `BUENAS_PRACTICAS.md` y skill `capas-spring-boot` / `front-react-tailwind`.
- Al escribir o cambiar tests: `TESTING.md` y skill `pruebas-unitarias`.
- Al tocar auth, datos personales, webhook, IA o despliegue: `SEGURIDAD.md` y skill `seguridad`.
- Al trabajar el bot: `BOT_SPEC.md`.
- Al trabajar precios o resúmenes de dinero: skill `precios-y-estadisticas`.

## Comandos (actualizar cuando existan)
- Backend: `cd backend && ./mvnw test` · `./mvnw spring-boot:run`
- Frontend: `cd frontend && npm install && npm run dev` · `npm run test` · `npm run lint` · `npm run build`
- Todo: `docker compose up --build`

## Reglas críticas (no negociables)
- La BD relacional es la ÚNICA fuente de verdad de citas y disponibilidad. La vectorial solo para búsqueda semántica.
- El bot nunca inventa horarios: siempre consulta herramientas del backend.
- El núcleo del proyecto son los TURNOS. Precios y estadísticas son secundarios: no empezarlos antes de E1-T8 (ver roadmap).
- El dinero lo calcula el backend (SQL + `BigDecimal`), nunca la IA. El bot solo lo consulta con tools y lo presenta.
- Cada cita guarda `price_charged` al crearse; cambiar el precio de un servicio no modifica citas anteriores.
- Toda escritura iniciada por el bot pide confirmación explícita antes de ejecutarse.
- No hay dobles reservas: validar solapamiento dentro de una transacción.
- Migraciones solo con Flyway; nunca editar una migración ya aplicada.
- Nunca commitear secretos. Variables de entorno + `.env.example`.
- No borrar citas: se cancelan (`CANCELLED`).
- Ninguna tarea se da por terminada sin pruebas unitarias de su lógica de negocio.
- Seguridad desde el inicio, no "al final": aplica el checklist de `SEGURIDAD.md` de la etapa en curso.
- Commit + push a GitHub después de cada tarea terminada (sin esperar aprobación para commitear).

## Buenas prácticas (resumen; detalle en `docs/BUENAS_PRACTICAS.md`)
- Código simple primero (KISS/YAGNI). Un patrón de diseño solo si resuelve un problema concreto de ESTA tarea,
  y dime cuál usas y por qué.
- Estructuras de datos estándar de Java/TS (List, Map, Set, TreeMap). Estructuras propias solo si son necesarias
  y justificadas con una frase.
- SOLID razonable, nombres claros, funciones cortas, sin código muerto ni comentarios obvios.

## Accesibilidad / UX
- Fuente base ≥ 18px, botones ≥ 48px, alto contraste, máx. 3 acciones por pantalla.
- Estados con color + texto + ícono. Vista principal = "Hoy".

## Definition of Done (por tarea)
- Compila, tests nuevos y existentes en verde, lint sin errores.
- Checklist de seguridad de la tarea revisado.
- Funciona en pantalla de celular (360px) si toca el front.
- `docs/PROGRESO.md` actualizado y reporte de avance entregado.
