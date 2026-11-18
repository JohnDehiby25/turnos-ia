# backend/

Backend Spring Boot 4 (Java 21, Maven) de Turnos IA. Paquete raíz: `com.turnosia`.

## Requisitos
- JDK 21, Maven (o el wrapper `./mvnw`), Docker (PostgreSQL 16 + pgvector).

## Desarrollo local
1. Desde la raíz del repo: `docker compose up -d` (levanta PostgreSQL).
2. Desde `backend/`: `./mvnw spring-boot:run` (Windows: `.\mvnw.cmd spring-boot:run`).

Configuración por variables de entorno (`DB_URL`, `DB_USER`, `DB_PASSWORD`); ver `.env.example` en la raíz.

## Pruebas
- `./mvnw test` — unitarias + integración (Testcontainers levanta PostgreSQL real).

## Migraciones
Flyway: `src/main/resources/db/migration/`. Nunca editar una migración ya aplicada.
Todo el esquema de la BD va aquí como migración (no hay carpeta `database/` en la raíz).
