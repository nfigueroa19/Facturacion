package com.hospital.facturacion.api.paciente

import com.hospital.facturacion.api.prefactura.CampoDto
import com.hospital.facturacion.api.prefactura.CitaDto
import com.hospital.facturacion.api.prefactura.NovedadDto
import com.hospital.facturacion.api.prefactura.PagoDto
import com.hospital.facturacion.api.prefactura.aDto
import com.hospital.facturacion.domain.formulario.ErrorCampo
import com.hospital.facturacion.domain.formulario.FormularioPrefactura
import com.hospital.facturacion.domain.prefactura.EstadoPrefactura
import com.hospital.facturacion.domain.prefactura.SolicitudPrefactura
import com.hospital.facturacion.domain.soporte.TipoSoporte
import com.hospital.facturacion.service.paciente.PrefacturaPaciente
import kotlinx.serialization.Serializable

@Serializable
data class AccesoPacienteDto(val identificacion: String, val codigoAcceso: String)

@Serializable
data class TokenDto(val token: String)

@Serializable
data class SolicitudPacienteDto(
    val consecutivo: String,
    val estado: EstadoPrefactura,
    val fechaEnvio: String,
    val datos: Map<String, String>,
    val novedades: List<NovedadDto>,
)

@Serializable
data class PrefacturaPacienteDto(
    val cita: CitaDto,
    val formulario: List<CampoDto>,
    val soportes: Set<TipoSoporte>,
    val pago: PagoDto?,
    val solicitud: SolicitudPacienteDto?,
    val tamanoMaximoSoporteBytes: Int,
)

@Serializable
data class DatosPacienteDto(val datos: Map<String, String>)

@Serializable
data class RadicacionDto(val consecutivo: String, val estado: EstadoPrefactura)

@Serializable
data class ErroresRadicacionDto(val errores: Map<String, ErrorCampo>, val soportesFaltantes: Set<TipoSoporte>)

fun PrefacturaPaciente.aDto(formulario: FormularioPrefactura, tamanoMaximoSoporteBytes: Int) = PrefacturaPacienteDto(
    cita = expediente.cita.aDto(),
    formulario = formulario.campos.map { it.aDto() },
    soportes = expediente.soportes,
    pago = expediente.pago?.aDto(),
    solicitud = solicitud?.aDtoPaciente(),
    tamanoMaximoSoporteBytes = tamanoMaximoSoporteBytes,
)

private fun SolicitudPrefactura.aDtoPaciente() =
    SolicitudPacienteDto(consecutivo.valor, estado, fechaEnvio.toString(), datos.valores, novedades.map { it.aDto() })
