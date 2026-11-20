# Requisitos

## Usuarios
- **Dueña del salón (admin)**: ve y gestiona turnos desde el panel web (celular) o preguntándole al bot por WhatsApp tambien puede cancelar citas, etc.
- **Cliente**: pide, consulta o cancela su turno hablando con el bot por WhatsApp (fase posterior), no conoce estadisticas de ganancias del admin.

## Tipos de cita (servicios, editables)
Mechas, Cepillado, Detalle (más los que se agreguen). Cada servicio tiene duración en minutos y precio opcional.

## Estados de una cita
| Código | Texto en pantalla | Significado |
|---|---|---|
| `SCHEDULED` | Por atender | Asignada, aún no ocurre |
| `ATTENDED` | Atendida | Ya se hizo |
| `CANCELLED` | Cancelada | Se canceló |
| `NO_SHOW` | No asistió | El cliente no llegó |

## Requisitos funcionales
- RF1. Ver los turnos de un día. Ejemplo: "dame los turnos del 18 de noviembre de 2026". Cada fila:
  nombre del cliente, hora programada, tipo de cita, estado, y fecha y hora en que se asignó la cita (`created_at`).
- RF2. Consultar espacios disponibles en un rango de fechas (opcionalmente según el servicio y su duración).
- RF3. Crear cita (cliente, servicio, fecha/hora) validando que no choque con otra ni caiga fuera del horario.
- RF4. Cancelar (sin ventana mínima de anticipación), reprogramar y marcar como atendida / no asistió.
- RF5. Horario semanal configurable y excepciones por día (abrir o cerrar una fecha puntual).
- RF6. **Domingo**: el sistema pregunta a la dueña (por WhatsApp/panel) si trabajará ese domingo antes de ofrecerlo
  a clientes; por defecto los domingos están CERRADOS hasta que ella confirme.
- RF7. Resumen del día / de la semana (cantidad de citas por estado).
- RF8. Bot conversacional (chat web y WhatsApp) que ejecuta RF1–RF7 con lenguaje natural.
- RF9. Búsqueda semántica sobre información del salón (precios, políticas, cuidados) para responder preguntas frecuentes.
- RF10. Historial: toda cita conserva `created_at`, `updated_at` y cambios de estado.

## Precios y estadísticas (secundario: el núcleo son los turnos)
- RF11. **Ver precios**: lista de servicios con su precio. El panel y el bot la muestran (los clientes también pueden consultarla).
  La dueña edita los precios desde el panel.
- RF12. Cada cita tiene precio obligatorio (`price_charged`), que la dueña fija al crearla porque depende de varios
  factores. El precio del servicio es solo referencia. Cambiar el precio de un servicio NO altera citas ya creadas.
- RF13. **Resumen por día, semana (lunes a domingo) y mes**, y por rango libre:
  - número de citas por estado,
  - **dinero ganado** = suma de `price_charged` de citas `ATTENDED`,
  - **dinero proyectado** = suma de citas `SCHEDULED`,
  - dinero no percibido = suma de `CANCELLED` + `NO_SHOW` (solo informativo),
  - servicios más pedidos,
  - propinas del periodo (suma de `tip` de citas `ATTENDED`, aparte del dinero de servicios).
- RF14. El bot responde en lenguaje natural ("¿cuánto cuesta un cepillado?", "¿cuánto gané esta semana?") llamando
  herramientas. El dinero siempre lo calcula el backend; la IA solo lo presenta.
- Montos en pesos colombianos (COP), mostrados como `$ 120.000`. Días, semanas y meses según `America/Bogota`.
- Las estadísticas son solo para la dueña (rol ADMIN).

## Requisitos no funcionales
- Usabilidad: mobile-first, letra grande, flujo de 1–2 toques para lo más común.
- Seguridad: login simple para la dueña; secretos por variables de entorno; HTTPS.
- Disponibilidad: desplegado en la nube con Docker; backups diarios de la BD.
- Rendimiento: respuestas de API < 500 ms (sin contar la IA).
- Mantenibilidad: capas claras, tests de reglas de negocio, migraciones Flyway.

## Fuera de alcance (por ahora)
Pagos en línea, contabilidad completa (gastos, impuestos, nómina), múltiples sucursales, múltiples estilistas con agendas separadas (incluida la manicurista), clientes que pagan a fin de mes (cuentas por cobrar), app nativa iOS/Android
(se usa PWA instalable).

## Decisiones de la dueña (E0-T2, 2026-11-18)
- **Horario**: no hay horario fijo; depende de la disponibilidad. Suele haber hora de almuerzo,
  pero en días muy agendados (sábados, temporadas altas como diciembre o marzo) se omite.
  → Horario semanal configurable con almuerzo opcional por día, ajustable por fecha.
- **Atención**: solo atiende UNA persona (la dueña). La manicurista es tema aparte (fuera de alcance).
  → Una sola agenda, sin citas paralelas.
- **Duración y precio de los servicios**: dependen del largo y estado del cabello y de una
  asesoría previa. → Cada cita define su duración (start/end) y su precio (`price_charged`) por caso;
  el servicio solo da valores estimados por defecto.
- **Cancelación**: sin ventana mínima; el cliente cancela cuando quiere (son casos extraños).
  → No se impone regla de anticipación.
- **Cobro**: se cobra al terminar el servicio. Algunos clientes pagan a fin de mes todos sus
  cepillados de la semana (casos extraordinarios → fuera de alcance, anotado en Ideas).
- **Descuentos**: no hay descuentos formales; el precio se define por caso (estado del cabello,
  situación del cliente). → `price_charged` ajustable por cita, sin campo de descuento.
- **Propinas**: se registran si el cliente las da (voluntarias). → `appointment.tip` opcional.
