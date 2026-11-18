-- V2 (E1-T5a): cliente con nombre y apellido; teléfono opcional.
-- El apellido de clientes existentes queda vacío; la aplicación exige que se llene al editar.

ALTER TABLE client RENAME COLUMN name TO first_name;
ALTER TABLE client ADD COLUMN last_name VARCHAR(120) NOT NULL DEFAULT '';
ALTER TABLE client ALTER COLUMN last_name DROP DEFAULT;
ALTER TABLE client ALTER COLUMN phone DROP NOT NULL;
