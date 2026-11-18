---
name: pruebas-unitarias
description: Cómo escribir pruebas unitarias y de integración en este proyecto (JUnit, Mockito, Testcontainers, Vitest). Cargar al crear o modificar tests o lógica de negocio.
---

# Pruebas

- Seguir `docs/TESTING.md`.
- Backend: JUnit 5 + Mockito + AssertJ; `Clock` inyectado; sin Spring en pruebas unitarias de `service`.
- Nombres `metodo_condicion_resultado`; patrón Given/When/Then.
- Cubrir caso feliz, bordes y errores. Cada bug corregido deja un test.
- LLM siempre simulado. BD real solo con Testcontainers.
- Ejecutar `./mvnw test` (o `npm run test`) y reportar el resultado en el reporte de avance.
