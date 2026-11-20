-- V4: toda cita tiene precio (lo fija el admin al crearla). Si hay citas antiguas sin precio, esta migración falla y no pierde datos.
ALTER TABLE appointment
    ALTER COLUMN price_charged SET NOT NULL;
