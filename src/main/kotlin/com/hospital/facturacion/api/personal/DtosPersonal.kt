package com.hospital.facturacion.api.personal

import com.hospital.facturacion.api.prefactura.CitaDto
import com.hospital.facturacion.api.prefactura.NovedadDto
import com.hospital.facturacion.api.prefactura.aDto
import com.hospital.facturacion.domain.cita.DatosNuevaCita
import com.hospital.facturacion.domain.cita.ErrorNuevaCita
import com.hospital.facturacion.domain.cita.Regimen
import com.hospital.facturacion.domain.comunicacion.Comunicacion
import com.hospital.facturacion.domain.comunicacion.EstadoEnvio
import com.hospital.facturacion.domain.comunicacion.MotivoComunicacion
import com.hospital.facturacion.domain.digiturno.DestinoTurno
import com.hospital.facturacion.domain.digiturno.MotivoVentanilla
import com.hospital.facturacion.domain.prefactura.EstadoPrefactura
import com.hospital.facturacion.service.callcenter.ResultadoProgramacion
import com.hospital.facturacion.service.digiturno.ResultadoDigiturno
import kotlinx.serialization.Serializable

@Serializable
data class NuevaCitaDto(
    val identificacion: String,
    val celular: String,
    val fecha: String,
    val medico: String,
    val especialidad: String,
    val regimen: Regimen,
    val copagoPesos: Long? = null,
)

@Serializable
data class ComunicacionDto(
    val cita: String,
    val motivo: MotivoComunicacion,
    val destino: String,
    val texto: String,
    val estado: EstadoEnvio,
    val canal: String,
    val fecha: String,
)

@Serializable
data class CitaProgramadaDto(val identificacion: String, val cita: CitaDto, val codigoAcceso: String, val sms: ComunicacionDto?)

@Serializable
data class ErroresCitaDto(val errores: Set<ErrorNuevaCita>)

@Serializable
data class LecturaDigiturnoDto(val identificacion: String)

@Serializable
data class TurnoDto(
    val turno: String,
    val destino: DestinoTurno,
    val motivo: MotivoVentanilla?,
    val cita: CitaDto?,
    val consecutivo: String?,
    val estado: EstadoPrefactura?,
    val novedades: List<NovedadDto>,
)

fun NuevaCitaDto.aDominio() = DatosNuevaCita(identificacion, celular, fecha, medico, especialidad, regimen, copagoPesos)

fun Comunicacion.aDto() =
    ComunicacionDto(cita.valor, motivo, mensaje.destino.valor, mensaje.texto, envio.estado, envio.canal, fecha.toString())

fun ResultadoProgramacion.Programada.aDto() = CitaProgramadaDto(cita.identificacionPaciente, cita.aDto(), codigoAcceso, sms?.aDto())

fun ResultadoDigiturno.aDto(): TurnoDto = when (this) {
    is ResultadoDigiturno.Preconsulta ->
        TurnoDto(turno.codigo, turno.destino, null, cita.aDto(), solicitud.consecutivo.valor, solicitud.estado, emptyList())
    is ResultadoDigiturno.Ventanilla ->
        TurnoDto(turno.codigo, turno.destino, motivo, cita?.aDto(), null, null, novedades.map { it.aDto() })
}
