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
- RF4. Cancelar, reprogramar y marcar como atendida / no asistió.
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
- RF12. Cada cita guarda el precio cobrado (`price_charged`) al crearse: copia del precio del servicio, ajustable
  (descuento, caso especial). Cambiar el precio de un servicio NO altera citas ya creadas.
- RF13. **Resumen por día, semana (lunes a domingo) y mes**, y por rango libre:
  - número de citas por estado,
  - **dinero ganado** = suma de `price_charged` de citas `ATTENDED`,
  - **dinero proyectado** = suma de citas `SCHEDULED`,
  - dinero no percibido = suma de `CANCELLED` + `NO_SHOW` (solo informativo),
  - servicios más pedidos.
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
Pagos en línea, contabilidad completa (gastos, impuestos, nómina), múltiples sucursales, múltiples estilistas con agendas separadas, app nativa iOS/Android
(se usa PWA instalable).

## Preguntas abiertas (resolver con la dueña)
- ¿Horario por defecto? ¿Hay hora de almuerzo?
  El horario depende de la disponibilidad, regularmente hay hora de almuerzo, sin embargo en dias muy agendados no es posible como sabados o temporadas altas como diciembre o marzo
- ¿Una sola persona atiende o varias a la vez?
  Solo una persona atiende, la idea es basarlo solo en la dueña del negocio, aunque hay una manicurista,pero eso es tema aparte
- ¿Duración real de mechas / cepillado / detalle?
  Los servicios depende del largor, estado del cabello, y asesoramiento 
  previamente para determinar un precio y tiempo estimado
- ¿Con cuánta anticipación se puede cancelar?
  La anticipacion es decision del cliente, aunque generalmente son casos extraños esos
- ¿Cuáles son los precios actuales? ¿Son fijos o varían (ej. mechas según el largo del cabello)? ¿Se cobra al terminar?
  Se cobra al terminar, sin embargo hay uno que otro cliente que paga es a fin de mes todos los cepillados de las semanas, son casos extraordinarios
- ¿Hay descuentos o propinas que deban registrarse?
  Los descuentos normalmente no los hay, mas que nada se genera el precio del servicio por varios factores, como situacion economica del cliente pero mayoritariamente es por el estado del cabello y caracteristicas asi, propinas se registran no son obligatorias, hay casos q los clientes dan propina por decision propia
