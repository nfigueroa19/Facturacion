package com.hospital.facturacion.api.facturador

import com.hospital.facturacion.domain.prefactura.Atencion
import com.hospital.facturacion.domain.prefactura.Registro
import kotlinx.serialization.Serializable

@Serializable
data class RegistroDto(val valor: String, val facturador: String, val fecha: String)

@Serializable
data class AtencionDto(
    val ingreso: RegistroDto?,
    val intentosFallidos: List<RegistroDto>,
    val factura: RegistroDto?,
    val anulacion: RegistroDto?,
)

/** Número de ingreso o de factura que el facturador copia desde Dinámica. */
@Serializable
data class NumeroDinamicaDto(val numero: String)

/** Motivo de anulación o error de facturación devuelto por Dinámica. */
@Serializable
data class ObservacionDto(val texto: String)

fun Atencion.aDto() = AtencionDto(
    ingreso = ingreso?.aDto { it.valor },
    intentosFallidos = intentosFallidos.map { registro -> registro.aDto { it.texto } },
    factura = factura?.aDto { it.valor },
    anulacion = anulacion?.aDto { it.texto },
)

private fun <T> Registro<T>.aDto(texto: (T) -> String) = RegistroDto(texto(valor), facturador, fecha.toString())
