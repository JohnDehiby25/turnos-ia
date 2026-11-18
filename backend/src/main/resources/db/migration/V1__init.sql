-- V1: esquema inicial del núcleo de turnos (E1, sin IA).
-- knowledge_chunk (pgvector) llega en E3 con su propia migración.

CREATE TABLE client (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(120) NOT NULL,
    phone      VARCHAR(20)  NOT NULL UNIQUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE service_type (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(120)   NOT NULL,
    duration_min INTEGER        NOT NULL CHECK (duration_min > 0),
    price        NUMERIC(12, 2) NULL, -- NULL = "precio por confirmar"
    active       BOOLEAN        NOT NULL DEFAULT TRUE
);

CREATE TABLE appointment (
    id              BIGSERIAL PRIMARY KEY,
    client_id       BIGINT        NOT NULL REFERENCES client (id),
    service_type_id BIGINT        NOT NULL REFERENCES service_type (id),
    start_at        TIMESTAMPTZ   NOT NULL,
    end_at          TIMESTAMPTZ   NOT NULL,
    status          VARCHAR(20)   NOT NULL DEFAULT 'SCHEDULED'
        CHECK (status IN ('SCHEDULED', 'ATTENDED', 'CANCELLED', 'NO_SHOW')),
    price_charged   NUMERIC(12, 2) NULL, -- precio real de la cita (por caso)
    tip             NUMERIC(12, 2) NULL, -- propina opcional
    notes           TEXT,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT appointment_end_after_start CHECK (end_at > start_at)
);

CREATE INDEX idx_appointment_start_at ON appointment (start_at);

-- Anti-solapamiento (una sola agenda). Parcial: las citas canceladas no bloquean.
-- Citas contiguas (una termina cuando empieza la otra) sí se permiten ([start, end)).
ALTER TABLE appointment
    ADD CONSTRAINT appointment_no_overlap
    EXCLUDE USING gist (tstzrange(start_at, end_at) WITH &&) WHERE (status <> 'CANCELLED');

CREATE TABLE working_hours (
    id          BIGSERIAL PRIMARY KEY,
    weekday     SMALLINT NOT NULL UNIQUE CHECK (weekday BETWEEN 1 AND 7), -- 1=Lunes..7=Domingo (java.time.DayOfWeek)
    is_open     BOOLEAN NOT NULL,
    open_time   TIME,
    close_time  TIME,
    lunch_start TIME, -- almuerzo opcional por día
    lunch_end   TIME,
    CONSTRAINT working_hours_open_requires_times
        CHECK (is_open = (open_time IS NOT NULL AND close_time IS NOT NULL))
);

CREATE TABLE day_override (
    date        DATE PRIMARY KEY,
    is_open     BOOLEAN NOT NULL,
    open_time   TIME,
    close_time  TIME,
    lunch_start TIME, -- NULL = sin almuerzo ese día
    lunch_end   TIME,
    note        VARCHAR(255)
);

-- Servicios iniciales. Duración estimada por defecto; el precio se define por caso → NULL.
INSERT INTO service_type (name, duration_min, price, active) VALUES
    ('Mechas', 180, NULL, TRUE),
    ('Cepillado', 45, NULL, TRUE),
    ('Detalle', 90, NULL, TRUE);
