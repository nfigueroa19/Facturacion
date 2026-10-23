-- Esquema inicial de la prefactura de consulta externa. Solo datos ficticios durante el PoC.
-- Las citas siguen en la agenda de prueba (config/citas-prueba.json); por eso `cita` no tiene llave foránea.

CREATE SEQUENCE consecutivo_radicado;

CREATE TABLE solicitud_prefactura (
    consecutivo           TEXT PRIMARY KEY,
    cita                  TEXT        NOT NULL UNIQUE,
    estado                TEXT        NOT NULL CHECK (estado IN
        ('EN_REVISION', 'PENDIENTE', 'APROBADO', 'INGRESO_ANULADO', 'LISTO_PARA_FACTURAR', 'FACTURADO')),
    fecha_envio           TIMESTAMPTZ NOT NULL,
    datos                 JSONB       NOT NULL,
    -- Novedades del facturador en orden: se leen y escriben siempre con su solicitud.
    novedades             JSONB       NOT NULL DEFAULT '[]',
    validacion_facturador TEXT,
    validacion_fecha      TIMESTAMPTZ,
    CHECK ((validacion_facturador IS NULL) = (validacion_fecha IS NULL))
);

-- El PDF se guarda sin modificar para no invalidar la firma digital de la EPS (P-21).
CREATE TABLE soporte (
    cita      TEXT  NOT NULL,
    tipo      TEXT  NOT NULL CHECK (tipo IN ('AUTORIZACION', 'ORDEN_MEDICA')),
    contenido BYTEA NOT NULL,
    PRIMARY KEY (cita, tipo)
);

CREATE TABLE pago_copago (
    cita        TEXT PRIMARY KEY,
    referencia  TEXT        NOT NULL,
    valor_pesos BIGINT      NOT NULL CHECK (valor_pesos > 0),
    fecha       TIMESTAMPTZ NOT NULL
);

-- Clave en formato pbkdf2-sha256:<iteraciones>:<sal>:<hash>; nunca en texto plano.
CREATE TABLE facturador (
    usuario    TEXT PRIMARY KEY,
    nombre     TEXT NOT NULL,
    hash_clave TEXT NOT NULL
);
