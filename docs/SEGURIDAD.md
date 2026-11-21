# Seguridad (checklist por etapa — marcar al terminar cada puerta)

## Principios
Mínimo privilegio · validar toda entrada · no confiar en el cliente ni en el LLM · secretos fuera del código · registrar sin exponer datos.

## E1 — Backend
- [x] Contraseña de la dueña con BCrypt; nunca en texto plano
- [x] Autenticación (JWT de corta duración o sesión) con Spring Security; todos los `/api/**` protegidos salvo login
- [x] Validación con `@Valid` en todos los DTOs; rechazar fechas pasadas, textos largos, IDs inexistentes
- [x] Sin SQL concatenado (solo JPA / parámetros)
- [x] CORS restringido al dominio del front (`ALLOWED_ORIGINS`)
- [ ] Usuario de BD con permisos mínimos (no superusuario) en producción → pendiente de E4 (despliegue)
- [x] Errores sin stack traces ni detalles internos al cliente
- [x] Secretos solo por variables de entorno; `.env` en `.gitignore`
- [x] Rate limiting en login (anti fuerza bruta) → 5 intentos en 15 min, bloqueo 15 min, por usuario y por IP
- [x] Edición de precios y `/api/stats/**` solo para rol ADMIN; consulta de precios es solo lectura
      → `PUT /api/services/**` y `/api/stats/**` exigen ADMIN en `SecurityConfig` (E1-T8, E1-T9)
- [x] CSRF activo en la API con cookie `XSRF-TOKEN` (el front la envía en la cabecera `X-XSRF-TOKEN`)

## E2 — Frontend
- [ ] Token no expuesto en logs; manejo de expiración
- [x] React escapa por defecto: prohibido `dangerouslySetInnerHTML` con datos de usuario
- [x] Rutas protegidas; no mostrar datos sin sesión (`RequireSession`, pruebas en `App.test.tsx`)

## E3 — IA
- [ ] El LLM solo ejecuta tools definidas, con parámetros validados por el `service` (nunca SQL ni código arbitrario)
- [ ] Permisos por rol aplicados en el backend, no en el prompt
- [ ] Mensajes del usuario tratados como datos: defensa contra prompt injection (no seguir instrucciones que cambien las reglas)
- [ ] Confirmación explícita antes de escrituras
- [ ] `get_income_summary` solo para la dueña; los clientes solo ven precios, nunca estadísticas ni datos de otros
- [ ] No enviar al proveedor del LLM más datos personales de los necesarios
- [ ] Límite de mensajes/tokens por usuario (costos y abuso)
- [ ] `knowledge_chunk` sin datos personales

## E4 — Nube
- [ ] HTTPS obligatorio; redirección de HTTP
- [ ] Backups automáticos de la BD y prueba de restauración
- [ ] Dependabot / escaneo de dependencias en GitHub
- [ ] Imágenes Docker mínimas, sin correr como root
- [ ] Variables de entorno en Railway; ninguna en el repo

## E5 — WhatsApp
- [ ] Verificar firma `X-Hub-Signature-256` del webhook antes de procesar
- [ ] Idempotencia: ignorar mensajes repetidos (mismo id)
- [ ] El cliente solo accede a sus citas (identificado por su número)
- [ ] Respetar consentimiento y opción de no recibir mensajes

## Datos personales (Colombia)
Se guardan nombre y teléfono de clientes: aplicar la Ley 1581 de 2012 (habeas data): informar para qué se usan los datos,
pedir autorización y permitir corregirlos o eliminarlos. Consultar con una persona experta antes de operar con clientes reales.

## Cada tarea
Antes de cerrar: ¿qué entrada no confiable toca esta tarea? ¿cómo la valido? ¿qué dato sensible maneja?
