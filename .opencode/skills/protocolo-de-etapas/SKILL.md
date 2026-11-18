---
name: protocolo-de-etapas
description: Cómo iniciar, avanzar y cerrar sesiones por etapas (retomar progreso, una tarea a la vez, reportar avances, actualizar PROGRESO.md). Cargar al empezar o cerrar una sesión y al terminar una tarea.
---

# Protocolo de etapas

## Inicio de sesión
1. Leer `docs/PROGRESO.md` y `docs/ROADMAP.md`.
2. Resumir en ≤ 5 líneas: etapa, hecho, pendiente, próxima tarea.
3. Preguntar UNA cosa: "¿Seguimos con <tarea> o prefieres otra cosa?" y esperar.

## Antes de codificar
Plan de 3–5 viñetas + archivos que se tocarán + pruebas que se escribirán. Esperar OK.

## Al terminar la tarea (reporte de avance)
1. Qué se hizo. 2. Cómo verlo. 3. Resultado de las pruebas. 4. Seguridad/buenas prácticas aplicadas. 5. Próxima tarea.
Actualizar `docs/PROGRESO.md` (estado, bitácora, decisiones, ideas). Preguntar: "¿Apruebo commit y seguimos?".

## Cierre de etapa
Verificar la puerta de salida del roadmap y el checklist de `docs/SEGURIDAD.md` de esa etapa. Presentar un resumen y
esperar aprobación antes de abrir la siguiente etapa.

## Prohibido
Adelantar etapas, hacer varias tareas juntas, cambiar el stack, o dar por terminado algo sin pruebas.

## Arquitectura y Diseño
Crea una carpeta donde haya carpetas con nombre diseño requerimientos
dentro debes generar una lista de graficas que añadan cada necesidad paso a paso sin saltarse etapas,actualizalo al pasar a un requerimiento

Crea una carpeta donde se llame arquitectura, donde añadas graficas, visibles e interactivas, del flujo entre diferentes herramientas y capas, utilizando el estilo arquitectonico definido

Crea una carpeta donde listes todos los requerimientos funcionales y no funcionales del sistema, los checks o unchecked, no pierdas el contexto y actualizalo cada ves una se complete
