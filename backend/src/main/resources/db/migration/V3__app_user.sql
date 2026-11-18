-- V3 (E1-T7): usuaria del sistema (la dueña). Contraseña con hash BCrypt, nunca en texto plano.

CREATE TABLE app_user (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'ADMIN' CHECK (role IN ('ADMIN')),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);
