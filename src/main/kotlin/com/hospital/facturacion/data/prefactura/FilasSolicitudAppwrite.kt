package com.hospital.facturacion.data.prefactura

import com.hospital.facturacion.data.appwrite.fechaAppwrite
import com.hospital.facturacion.data.appwrite.instanteAppwrite
import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.prefactura.ConsecutivoRadicado
import com.hospital.facturacion.domain.prefactura.DatosFacturacion
import com.hospital.facturacion.domain.prefactura.EstadoPrefactura
import com.hospital.facturacion.domain.prefactura.Novedad
import com.hospital.facturacion.domain.prefactura.SolicitudPrefactura
import com.hospital.facturacion.domain.prefactura.Validacion
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Fila de la tabla `solicitudes`. `datos` es el JSON de los valores del formulario. */
@Serializable
data class FilaSolicitud(
    val consecutivo: String,
    val cita: String,
    val estado: EstadoPrefactura,
    @SerialName("fecha_envio") val fechaEnvio: String,
    val datos: String,
    @SerialName("total_novedades") val totalNovedades: Int,
    @SerialName("validacion_facturador") val validacionFacturador: String?,
    @SerialName("validacion_fecha") val validacionFecha: String?,
)

/** Fila de la tabla `novedades`: `orden` es la posición de la novedad dentro de su solicitud. */
@Serializable
data class FilaNovedad(val solicitud: String, val orden: Int, val descripcion: String, val facturador: String, val fecha: String)

internal fun SolicitudPrefactura.fila(json: Json) = FilaSolicitud(
    consecutivo = consecutivo.valor,
    cita = cita.valor,
    estado = estado,
    fechaEnvio = fechaEnvio.fechaAppwrite(),
    datos = json.encodeToString(datos.valores),
    totalNovedades = novedades.size,
    validacionFacturador = validacion?.facturador,
    validacionFecha = validacion?.fecha?.fechaAppwrite(),
)

internal fun SolicitudPrefactura.filasNovedades(): List<FilaNovedad> = novedades.mapIndexed { orden, novedad ->
    FilaNovedad(consecutivo.valor, orden, novedad.descripcion, novedad.facturador, novedad.fecha.fechaAppwrite())
}

// Una novedad con orden >= total viene de un guardado que no terminó: la fila de la solicitud todavía no la cuenta.
internal fun FilaSolicitud.solicitud(novedades: List<FilaNovedad>, json: Json) = SolicitudPrefactura(
    consecutivo = ConsecutivoRadicado(consecutivo),
    cita = requireNotNull(CitaId.de(cita)) { "Cita inválida en $consecutivo" },
    estado = estado,
    fechaEnvio = fechaEnvio.instanteAppwrite(),
    datos = DatosFacturacion(json.decodeFromString(datos)),
    novedades = novedades.filter { it.orden < totalNovedades }.sortedBy { it.orden }.map { it.novedad() }
        .also { check(it.size == totalNovedades) { "Faltan novedades de $consecutivo" } },
    validacion = validacionFacturador?.let { Validacion(it, requireNotNull(validacionFecha) { "Validación sin fecha en $consecutivo" }.instanteAppwrite()) },
)

private fun FilaNovedad.novedad(): Novedad =
    requireNotNull(Novedad.de(descripcion, facturador, fecha.instanteAppwrite())) { "Novedad inválida en $solicitud" }
