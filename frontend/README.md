# frontend/

Panel web de Turnos IA: React + TypeScript + Vite + Tailwind (mobile-first, PWA).

Comandos (desde `frontend/`):
- `npm install`
- `npm run dev`: servidor en `http://localhost:5173`. Las peticiones a `/api` se envían al backend en `localhost:8080`.
- `npm test`, `npm run lint`, `npm run build`

El cliente de la API está en `src/api/`. Los tipos reflejan los DTOs de `backend/src/main/java/com/turnosia/dto`.
