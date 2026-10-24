package com.hospital.facturacion.service.callcenter

import com.hospital.facturacion.domain.cita.Cita
import com.hospital.facturacion.domain.cita.DatosNuevaCita
import com.hospital.facturacion.domain.cita.ErrorNuevaCita
import com.hospital.facturacion.domain.cita.GeneradorAcceso
import com.hospital.facturacion.domain.cita.RegistroCitas
import com.hospital.facturacion.domain.cita.ValidacionNuevaCita
import com.hospital.facturacion.domain.cita.validar
import com.hospital.facturacion.domain.comunicacion.Comunicacion
import com.hospital.facturacion.domain.comunicacion.MotivoComunicacion
import com.hospital.facturacion.domain.comunicacion.NotificadorPaciente
import java.time.Clock
import java.time.LocalDate

sealed interface ResultadoProgramacion {
    /** `sms` es null solo si la cita no tiene celular; un envío fallido queda como FALLIDO dentro de la comunicación. */
    data class Programada(val cita: Cita, val codigoAcceso: String, val sms: Comunicacion?) : ResultadoProgramacion

    data class Invalida(val errores: Set<ErrorNuevaCita>) : ResultadoProgramacion
}

/** El call center asigna la cita, genera el ID de acceso y lo envía por SMS (flujo del paciente, pasos 2 y 3). */
class ProgramarCita(
    private val registro: RegistroCitas,
    private val generador: GeneradorAcceso,
    private val notificador: NotificadorPaciente,
    private val reloj: Clock,
) {
    fun ejecutar(datos: DatosNuevaCita): ResultadoProgramacion {
        val cita = when (val validacion = datos.validar(LocalDate.now(reloj), generador.citaId())) {
            is ValidacionNuevaCita.Invalida -> return ResultadoProgramacion.Invalida(validacion.errores)
            is ValidacionNuevaCita.Valida -> validacion.cita
        }
        val codigo = generador.codigoAcceso()
        registro.registrar(cita, codigo)
        val sms = notificador.notificar(cita, MotivoComunicacion.CITA_ASIGNADA, valoresCitaAsignada(cita, codigo))
        return ResultadoProgramacion.Programada(cita, codigo, sms)
    }
}

// Marcas disponibles en la plantilla `sms.CITA_ASIGNADA` de config/contenido.json.
private fun valoresCitaAsignada(cita: Cita, codigo: String) = mapOf(
    "fecha" to cita.fecha.toString(),
    "medico" to cita.medico,
    "especialidad" to cita.especialidad,
    "codigo" to codigo,
)
