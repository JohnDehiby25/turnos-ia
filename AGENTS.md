# Turnos IA — Asistente de turnos para salón de belleza

Sistema para gestionar los turnos de un salón pequeño. La dueña NO es técnica: todo simple, letra grande, pocos botones.
Un bot de IA atiende por WhatsApp y/o chat web; un panel web muestra las citas.

## Dónde está todo
- El repo git es `turnos-ia/` (origin `github.com/JohnDehiby25/turnos-ia`, rama `main`). La carpeta padre del workspace NO es repo: ejecuta `git` y los comandos de build desde `turnos-ia/`.
- Esta guía es la autoritativa. El estado (etapa, última y próxima tarea) vive SOLO en `docs/PROGRESO.md`; no lo copies aquí.
- `opencode.json` carga al inicio `docs/PROGRESO.md`, `ROADMAP.md`, `REQUISITOS.md` y `ARQUITECTURA.md`.
- `.env` (local, en `.gitignore`): NUNCA lo leas ni lo muestres. Usa `.env.example` como plantilla.

## Protocolo por etapas (lo más importante)
El desarrollador quiere ir paso a paso. NO construyas el sistema de una vez.

Al iniciar sesión:
1. Lee `docs/PROGRESO.md` y `docs/ROADMAP.md`.
2. Resume en máx. 5 líneas: etapa actual, hecho, a medias y próxima tarea.
3. Pregunta UNA cosa: "¿Seguimos con <próxima tarea> o prefieres otra cosa?". Espera respuesta antes de tocar código.

Durante la sesión:
- UNA tarea del roadmap a la vez. Si es grande, propón dividirla.
- No empieces otra etapa sin aprobación explícita (cada etapa tiene "puerta de salida" en `ROADMAP.md`).
- No agregues funcionalidad fuera de la tarea. Lo útil que aparezca va a `docs/PROGRESO.md > Ideas`.
- Antes de escribir código en una tarea nueva: plan en 3–5 viñetas y espera OK.

Al terminar una tarea, entrega un REPORTE DE AVANCE: (1) qué hiciste, (2) cómo verlo funcionando, (3) pruebas y resultado (`X passed`), (4) seguridad y buenas prácticas aplicadas, (5) próxima tarea sugerida. Luego actualiza `docs/PROGRESO.md`.

**Commit y push: SOLO con OK explícito del desarrollador.** Esta regla reemplaza la antigua "commit + push sin esperar aprobación". Formato de commit: `feat(E1-T2): ...`, `docs: ...`. El comando `/avance` (`.opencode/command/avance.md`) pide el OK.

Al cerrar sesión (si el desarrollador lo dice): deja en `docs/PROGRESO.md` un punto de control ("Estoy en…, falta…, siguiente paso…").

## Idioma
- Documentación, textos de UI y respuestas del bot: español (Colombia). Código, endpoints y commits: inglés.
- Zona horaria `America/Bogota`. En BD las fechas van en UTC y se muestran en hora local.

## Stack (no cambiar sin preguntar)
- Backend: Java 21, Spring Boot 4.1.2, Maven, Spring Web MVC, Spring Data JPA, Validation, Spring Security, Flyway. Spring AI todavía NO está en `pom.xml`.
- BD: PostgreSQL 16 + pgvector (SQL y vectores en una sola BD).
- Frontend (planeado): React + TypeScript + Vite + Tailwind, mobile-first, PWA.
- IA: LLM con tool calling + embeddings, proveedor configurable por variables de entorno.
- Canal: WhatsApp Cloud API (webhook) y chat web. Infra: Docker + compose, GitHub Actions, Railway.
- App móvil: etapa FINAL; solo se decide el enfoque (Capacitor vs. React Native/Expo) al llegar ahí.

## Backend (`backend/`, paquete `com.turnosia`)
- Paquetes existentes en `src/main/java/com/turnosia/`: `config`, `controller`, `service`, `repository`, `domain`, `dto`. `ai/` e `integration.whatsapp` aún NO existen.
- Dependencias solo hacia abajo: `controller → service → repository → domain`. Controladores sin lógica de negocio ni repositorios. Las tools de `ai` (cuando exista) llaman a `service`, nunca a `repository`.
- Esquema controlado por Flyway (`backend/src/main/resources/db/migration/`, V1–V3). `ddl-auto: validate`: Hibernate no crea tablas. Cambio de esquema = migración nueva (V4…). Nunca editar una migración ya aplicada.
- `open-in-view: false`: relaciones que se usan en DTOs se cargan de forma ansiosa (ver `PROGRESO.md`).
- La BD rechaza solapes con la restricción `appointment_no_overlap` (EXCLUDE gist, solo citas no `CANCELLED`). Citas contiguas [start, end) sí se permiten. La validación del service NO reemplaza esa restricción.
- Dinero: `BigDecimal` / `NUMERIC(12,2)`. En `service_type`, `price` NULL = "por confirmar".
- Tiempo: las respuestas de la API van en UTC (`Instant`); las peticiones exigen zona (`OffsetDateTime`). Para la hora actual inyecta `Clock` (bean en `config/ClockConfig`), no uses la hora real.

## Comandos
Backend (desde `backend/`; en Windows usa `.\mvnw.cmd` en vez de `./mvnw`):
- `./mvnw spring-boot:run`: necesita la BD arriba y `DB_PASSWORD` en `../.env` (la app lo importa sola). Sin `DB_PASSWORD` falla con "password authentication failed".
- `./mvnw test`: suite completa. Requiere **Docker daemon encendido**: Testcontainers levanta `pgvector/pgvector:pg16`. Si fallan al arrancar, es Docker, no el código.
- Una clase: `./mvnw test -Dtest=ClientRepositoryTest`.

Base de datos (desde `turnos-ia/`):
- `docker compose up -d`: levanta solo `db` (pgvector pg16 en `127.0.0.1:5432`). Exige `DB_PASSWORD` en `.env`.
- `docker compose up --build` NO funciona todavía: el compose solo tiene `db`. Backend y frontend en contenedores llegan en E4.

Frontend (desde `frontend/`, desde E2-T1): `npm install`, `npm run dev|test|lint|build`. Verifica `package.json` antes de asumir scripts.

Orden de verificación antes de dar una tarea por terminada: compila → tests (nuevos y existentes) → lint.

## Pruebas
- JUnit 5, Mockito, AssertJ, MockMvc, Testcontainers. Ubicación: `backend/src/test/java/com/turnosia/`.
- Integración con BD real: `@Import(TestcontainersConfiguration.class)` (copia el patrón existente).
- Spring Boot 4: `@DataJpaTest` está en `org.springframework.boot.data.jpa.test.autoconfigure` y `@AutoConfigureTestDatabase` en `org.springframework.boot.jdbc.test.autoconfigure`. No uses los paquetes de Boot 3.
- Unitarias obligatorias para cada regla de negocio en `service` (sin Spring ni BD, repositorios con Mockito). Ninguna tarea termina sin ellas.
- Nombres: `metodo_condicion_resultadoEsperado`. Sin depender de la hora real.
- El LLM SIEMPRE se simula en tests.
- Bug = primero un test que lo reproduce, luego el arreglo.
- Detalle: `docs/TESTING.md` y skill `pruebas-unitarias`.

## Reglas críticas (no negociables)
- La BD relacional es la ÚNICA fuente de verdad de citas y disponibilidad. La vectorial solo sirve para búsqueda semántica.
- El bot nunca inventa horarios: siempre consulta tools del backend.
- El núcleo son los TURNOS. Precios y estadísticas son secundarios: no empezarlos antes de E1-T8.
- El dinero lo calcula el backend (SQL + `BigDecimal`), nunca la IA. El bot solo consulta y presenta.
- Cada cita guarda `price_charged` al crearse; cambiar el precio de un servicio no toca citas anteriores.
- Toda escritura iniciada por el bot pide confirmación explícita antes de ejecutarse.
- No hay dobles reservas: validar solapamiento dentro de una transacción.
- No se borran citas: se cancelan (`CANCELLED`).
- Nunca commitear secretos: variables de entorno + `.env.example`.
- Seguridad desde el inicio: aplica el checklist de `docs/SEGURIDAD.md` de la etapa en curso. Al tocar auth, datos personales, webhook, IA o despliegue, usa también la skill `seguridad`.
- Commit y push solo con OK explícito (ver Protocolo).

## Dónde leer más
- Backend o frontend: `docs/BUENAS_PRACTICAS.md`, skills `capas-spring-boot` / `front-react-tailwind`.
- Agenda (solapes, horarios, domingos): skill `reglas-de-agenda`. Precios y resúmenes: skill `precios-y-estadisticas`.
- Bot: `docs/BOT_SPEC.md`.

## Buenas prácticas (resumen)
- Código simple primero (KISS/YAGNI). Un patrón de diseño solo si resuelve un problema concreto de la tarea, y di cuál y por qué.
- Estructuras estándar de Java/TS. Sin código muerto ni comentarios obvios.

## Accesibilidad / UX
- Fuente base ≥ 18px, botones ≥ 48px, alto contraste, máx. 3 acciones por pantalla.
- Estados con color + texto + ícono. Vista principal = "Hoy".

## Definition of Done (por tarea)
- Compila, tests nuevos y existentes en verde, lint sin errores (cuando exista frontend).
- Checklist de seguridad de la tarea revisado.
- Si toca el front: funciona en pantalla de 360px.
- `docs/PROGRESO.md` actualizado y reporte de avance entregado.
- Commit y push solo tras el OK del desarrollador.
