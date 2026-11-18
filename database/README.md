# database/

SQL y scripts de base de datos que NO se gestionan con Flyway.

- `init/` (futuro): scripts montados en `/docker-entrypoint-initdb.d` para ejecutar
  al levantar el contenedor PostgreSQL (p. ej. habilitar la extensión `pgvector`).
- Migraciones del esquema de la aplicación: van en `backend/src/main/resources/db/migration/`
  (Flyway), NO aquí.

Se llena en E1 (backend) según se necesite.
