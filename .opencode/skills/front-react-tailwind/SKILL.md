---
name: front-react-tailwind
description: Convenciones del frontend React + TypeScript + Tailwind, mobile-first y accesible para usuaria no técnica. Cargar al crear o editar pantallas.
---

# Front React + TS + Tailwind

- TypeScript estricto (`strict: true`), sin `any`.
- Cliente de API tipado en `src/api/`; tipos compartidos en `src/types/`.
- Mobile-first: diseñar para 360px y escalar. Texto base `text-lg`, botones `min-h-12`.
- Estados de cita con color + texto + ícono: Por atender (azul), Atendida (verde), Cancelada (rojo), No asistió (gris).
- Pantalla principal "Hoy": lista por hora, botón grande "Nueva cita".
- Siempre mostrar estados de carga, vacío y error en español simple.
- Sin lógica de negocio en el front; solo presentación y llamadas a la API.

# Diseño

-Utiliza colores azules con negro y blanco
-Utiliza skills como frontend-design o alguna instalada o pregunta por cual opcion descargar
-No haz diseños genericos, recomiendame estilos para hacerlo, asiatico, 
o algun estilo mas formal pero dinamico
-Hazlo accesible y usable 
-Interactivo, botones, graficas todo bien atractivo para el usuario de ver