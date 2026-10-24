package com.hospital.facturacion.data.prefactura

import com.hospital.facturacion.data.postgres.instante
import com.hospital.facturacion.data.postgres.utc
import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.prefactura.ConsecutivoRadicado
import com.hospital.facturacion.domain.prefactura.DatosFacturacion
import com.hospital.facturacion.domain.prefactura.EstadoPrefactura
import com.hospital.facturacion.domain.prefactura.Novedad
import com.hospital.facturacion.domain.prefactura.SolicitudPrefactura
import com.hospital.facturacion.domain.prefactura.Validacion
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.sql.ResultSet
import java.time.Instant

internal const val GUARDAR_SOLICITUD = """
    INSERT INTO solicitud_prefactura
        (consecutivo, cita, estado, fecha_envio, datos, novedades, validacion_facturador, validacion_fecha, atencion)
    VALUES (?, ?, ?, ?, CAST(? AS jsonb), CAST(? AS jsonb), ?, ?, CAST(? AS jsonb))
    ON CONFLICT (consecutivo) DO UPDATE SET estado = EXCLUDED.estado, fecha_envio = EXCLUDED.fecha_envio,
        datos = EXCLUDED.datos, novedades = EXCLUDED.novedades,
        validacion_facturador = EXCLUDED.validacion_facturador, validacion_fecha = EXCLUDED.validacion_fecha,
        atencion = EXCLUDED.atencion
"""

/** Forma de cada novedad dentro de la columna JSONB `novedades`. */
@Serializable
internal data class NovedadFila(val descripcion: String, val facturador: String, val fecha: String)

internal fun SolicitudPrefactura.columnas(json: Json): Array<Any?> = arrayOf(
    consecutivo.valor,
    cita.valor,
    estado.name,
    fechaEnvio.utc(),
    json.encodeToString(datos.valores),
    json.encodeToString(novedades.map { NovedadFila(it.descripcion, it.facturador, it.fecha.toString()) }),
    validacion?.facturador,
    validacion?.fecha?.utc(),
    atencion.json(json),
)

internal fun ResultSet.solicitud(json: Json): SolicitudPrefactura {
    val consecutivo = getString("consecutivo")
    val validador = getString("validacion_facturador")
    return SolicitudPrefactura(
        consecutivo = ConsecutivoRadicado(consecutivo),
        cita = requireNotNull(CitaId.de(getString("cita"))) { "Cita inválida en $consecutivo" },
        estado = EstadoPrefactura.valueOf(getString("estado")),
        fechaEnvio = requireNotNull(instante("fecha_envio")),
        datos = DatosFacturacion(json.decodeFromString(getString("datos"))),
        novedades = json.decodeFromString<List<NovedadFila>>(getString("novedades")).map { it.novedad(consecutivo) },
        validacion = validador?.let { Validacion(it, requireNotNull(instante("validacion_fecha"))) },
        atencion = atencionDe(getString("atencion"), json, consecutivo),
    )
}

private fun NovedadFila.novedad(consecutivo: String): Novedad =
    requireNotNull(Novedad.de(descripcion, facturador, Instant.parse(fecha))) { "Novedad inválida en $consecutivo" }
