---
name: seguridad
description: Checklist de seguridad por etapa (auth, validación, secretos, IA, webhook de WhatsApp, datos personales). Cargar al tocar autenticación, datos de clientes, IA, webhook o despliegue.
---

# Seguridad

1. Abrir `docs/SEGURIDAD.md` y revisar la sección de la etapa actual.
2. Para la tarea en curso responder: ¿qué entrada no confiable entra? ¿cómo se valida? ¿qué dato sensible se maneja?
3. Nunca confiar en el LLM ni en el cliente: permisos y validaciones en el backend.
4. Nunca commitear secretos ni loguear datos personales.
5. Marcar los ítems cumplidos en `SEGURIDAD.md` y mencionarlos en el reporte de avance.
6. No dejar datos sensibles en las llamadas del api cuando se muestre o lleve datos sensibles de la base de datos
7. No permitas que el servidor del backend tenga peticiones ilimitadas
8. Sigue las reglas que dicta OWASP 
9. No dejes datos o acceso de algo particular del servidor a la vista del frontend como en typescript
