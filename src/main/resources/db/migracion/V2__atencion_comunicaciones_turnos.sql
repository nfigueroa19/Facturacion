-- Fase 10: atención el día de la cita, bandeja de comunicaciones y turnos del digiturno. Solo datos ficticios.

-- Ingreso, intentos de facturación, factura y anulación de la solicitud (JSON de FilaAtencion).
ALTER TABLE solicitud_prefactura ADD COLUMN atencion JSONB;

-- SMS enviados o simulados, para el panel de auditoría del personal.
CREATE TABLE comunicacion (
    id      BIGSERIAL PRIMARY KEY,
    cita    TEXT        NOT NULL,
    motivo  TEXT        NOT NULL CHECK (motivo IN ('CITA_ASIGNADA', 'NOVEDAD_REGISTRADA')),
    destino TEXT        NOT NULL,
    texto   TEXT        NOT NULL,
    estado  TEXT        NOT NULL CHECK (estado IN ('ENVIADO', 'SIMULADO', 'FALLIDO')),
    canal   TEXT        NOT NULL,
    fecha   TIMESTAMPTZ NOT NULL
);

CREATE INDEX comunicacion_por_fecha ON comunicacion (fecha DESC);

CREATE SEQUENCE turno_preconsulta;
CREATE SEQUENCE turno_facturacion;
