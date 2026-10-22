package com.hospital.facturacion.api.prefactura

import com.hospital.facturacion.domain.cita.Cita
import com.hospital.facturacion.domain.cita.Regimen
import com.hospital.facturacion.domain.copago.PagoCopago
import com.hospital.facturacion.domain.formulario.CampoFormulario
import com.hospital.facturacion.domain.formulario.TipoCampo
import com.hospital.facturacion.domain.prefactura.Novedad
import kotlinx.serialization.Serializable

/** DTOs que comparten la web del paciente y el panel del facturador. Fechas en ISO 8601. */
@Serializable
data class CitaDto(
    val fecha: String,
    val medico: String,
    val especialidad: String,
    val regimen: Regimen,
    val copagoPesos: Long?,
)

@Serializable
data class CampoDto(val clave: String, val etiqueta: String, val tipo: TipoCampo, val obligatorio: Boolean, val ayuda: String?)

@Serializable
data class PagoDto(val referencia: String, val valorPesos: Long, val fecha: String)

@Serializable
data class NovedadDto(val descripcion: String, val fecha: String)

fun Cita.aDto() = CitaDto(fecha.toString(), medico, especialidad, regimen, copago?.valorPesos)

fun CampoFormulario.aDto() = CampoDto(clave, etiqueta, tipo, obligatorio, ayuda)

fun PagoCopago.aDto() = PagoDto(referencia, copago.valorPesos, fecha.toString())

fun Novedad.aDto() = NovedadDto(descripcion, fecha.toString())
