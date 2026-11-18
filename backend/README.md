# backend/

Backend Spring Boot 4 (Java 21, Maven) de Turnos IA. Paquete raíz: `com.turnosia`.

## Requisitos
- JDK 21, Maven (o el wrapper `./mvnw`), Docker (PostgreSQL 16 + pgvector).

## Desarrollo local
1. Desde la raíz del repo: `docker compose up -d` (levanta PostgreSQL).
2. Desde `backend/`: `./mvnw spring-boot:run` (Windows: `.\mvnw.cmd spring-boot:run`).

Configuración por variables de entorno (`DB_URL`, `DB_USER`, `DB_PASSWORD`); ver `.env.example` en la raíz.
`DB_PASSWORD` no tiene valor por defecto: defínela en la terminal antes de `spring-boot:run` con el mismo valor de tu `.env`
(Spring Boot no lee `.env` por sí solo). Si no está definida, el arranque falla con
`password authentication failed for user "turnosia"`: no es que la contraseña de la BD esté mal, es que falta la variable.

## Pruebas
- `./mvnw test` — unitarias + integración (Testcontainers levanta PostgreSQL real).

## Migraciones
Flyway: `src/main/resources/db/migration/`. Nunca editar una migración ya aplicada.
Todo el esquema de la BD va aquí como migración (no hay carpeta `database/` en la raíz).
