package com.hospital.facturacion.data.prefactura

import com.hospital.facturacion.domain.prefactura.Atencion
import com.hospital.facturacion.domain.prefactura.NumeroDinamica
import com.hospital.facturacion.domain.prefactura.Observacion
import com.hospital.facturacion.domain.prefactura.Registro
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.Instant

/** JSON de la columna `atencion`: ingreso, intentos fallidos, factura y anulación de una solicitud. */
@Serializable
internal data class FilaAtencion(
    val ingreso: FilaRegistro? = null,
    val intentosFallidos: List<FilaRegistro> = emptyList(),
    val factura: FilaRegistro? = null,
    val anulacion: FilaRegistro? = null,
)

@Serializable
internal data class FilaRegistro(val valor: String, val facturador: String, val fecha: String)

internal fun Atencion.json(json: Json): String = json.encodeToString(
    FilaAtencion(ingreso?.fila { it.valor }, intentosFallidos.map { r -> r.fila { it.texto } }, factura?.fila { it.valor }, anulacion?.fila { it.texto }),
)

/** Las solicitudes guardadas antes de la Fase 10 no tienen la columna: su atención está vacía. */
internal fun atencionDe(valor: String?, json: Json, consecutivo: String): Atencion {
    val fila = valor?.let { json.decodeFromString<FilaAtencion>(it) } ?: return Atencion()
    fun numero(r: FilaRegistro) = r.registro { requireNotNull(NumeroDinamica.de(it)) { "Número de Dinámica inválido en $consecutivo" } }
    fun observacion(r: FilaRegistro) = r.registro { requireNotNull(Observacion.de(it)) { "Observación inválida en $consecutivo" } }
    return Atencion(fila.ingreso?.let(::numero), fila.intentosFallidos.map(::observacion), fila.factura?.let(::numero), fila.anulacion?.let(::observacion))
}

private fun <T> Registro<T>.fila(texto: (T) -> String) = FilaRegistro(texto(valor), facturador, fecha.toString())

private fun <T> FilaRegistro.registro(valorDe: (String) -> T) = Registro(valorDe(valor), facturador, Instant.parse(fecha))
