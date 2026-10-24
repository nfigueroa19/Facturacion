-- Fase 12: SMS con el consecutivo al radicar la solicitud.
ALTER TABLE comunicacion DROP CONSTRAINT comunicacion_motivo_check;
ALTER TABLE comunicacion ADD CONSTRAINT comunicacion_motivo_check
    CHECK (motivo IN ('CITA_ASIGNADA', 'SOLICITUD_RADICADA', 'NOVEDAD_REGISTRADA'));
