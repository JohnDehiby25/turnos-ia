# Diagrama de base de datos (PostgreSQL)

```mermaid
erDiagram
    CLIENT ||--o{ APPOINTMENT : tiene
    SERVICE_TYPE ||--o{ APPOINTMENT : clasifica

    CLIENT {
        bigserial id PK
        varchar name
        varchar phone "UNIQUE"
        timestamptz created_at
    }

    SERVICE_TYPE {
        bigserial id PK
        varchar name
        integer duration_min "estimada por defecto"
        numeric price "NULL = por confirmar"
        boolean active
    }

    APPOINTMENT {
        bigserial id PK
        bigint client_id FK
        bigint service_type_id FK
        timestamptz start_at "índice"
        timestamptz end_at
        varchar status "SCHEDULED|ATTENDED|CANCELLED|NO_SHOW"
        numeric price_charged "precio real de la cita"
        numeric tip "propina, opcional"
        text notes
        timestamptz created_at
        timestamptz updated_at
    }

    WORKING_HOURS {
        bigserial id PK
        smallint weekday UK "1=Lun..7=Dom"
        boolean is_open
        time open_time
        time close_time
        time lunch_start "opcional"
        time lunch_end "opcional"
    }

    DAY_OVERRIDE {
        date date PK
        boolean is_open
        time open_time "NULL si cerrado"
        time close_time "NULL si cerrado"
        time lunch_start "NULL = sin almuerzo"
        time lunch_end "NULL = sin almuerzo"
        varchar note
    }

    KNOWLEDGE_CHUNK {
        bigserial id PK
        varchar source
        text content
        vector embedding "E3: pgvector"
        timestamptz created_at
    }
```

## Notas

- `APPOINTMENT`: restricción anti-solapamiento
  `EXCLUDE USING gist (tstzrange(start_at, end_at) WITH &&)` **parcial** (`status <> 'CANCELLED'`):
  una cita cancelada no bloquea el horario. Citas contiguas (una termina justo cuando empieza la otra) SÍ se permiten.
- `end_at > start_at` siempre (CHECK).
- `WORKING_HOURS`: si `is_open` es falso, los horarios van NULL. `weekday` 1=Lunes..7=Domingo (como `java.time.DayOfWeek`).
- `KNOWLEDGE_CHUNK` llega en **E3** con la extensión `pgvector`; no está en la migración V1 (E1 es sin IA).
- Fechas en UTC (`timestamptz`); se muestran en `America/Bogota`.
