package com.hospital.facturacion.service.digiturno

import com.hospital.facturacion.domain.cita.AgendaCitas
import com.hospital.facturacion.domain.cita.Cita
import com.hospital.facturacion.domain.cita.esHoy
import com.hospital.facturacion.domain.digiturno.DestinoTurno
import com.hospital.facturacion.domain.digiturno.GeneradorTurnos
import com.hospital.facturacion.domain.digiturno.MotivoVentanilla
import com.hospital.facturacion.domain.digiturno.Turno
import com.hospital.facturacion.domain.prefactura.Novedad
import com.hospital.facturacion.domain.prefactura.SolicitudPrefactura
import com.hospital.facturacion.domain.prefactura.SolicitudPrefacturaRepositorio
import java.time.Clock

sealed interface ResultadoDigiturno {
    val turno: Turno

    data class Preconsulta(override val turno: Turno, val cita: Cita, val solicitud: SolicitudPrefactura) : ResultadoDigiturno

    /** `novedades` informa al digiturno lo que el facturador marcó (RN-12). */
    data class Ventanilla(override val turno: Turno, val motivo: MotivoVentanilla, val cita: Cita?, val novedades: List<Novedad>) :
        ResultadoDigiturno
}

/**
 * El paciente lee su identificación en el digiturno el día de la cita. Con validación aprobada se confirma su llegada
 * (APROBADO a LISTO_PARA_FACTURAR, RN-14) y pasa a preconsulta; en cualquier otro caso va a facturación (RN-11).
 */
class AtenderLlegada(
    private val agenda: AgendaCitas,
    private val solicitudes: SolicitudPrefacturaRepositorio,
    private val turnos: GeneradorTurnos,
    private val reloj: Clock,
) {
    fun ejecutar(identificacion: String): ResultadoDigiturno {
        val cita = agenda.buscarPorIdentificacion(identificacion.trim()).firstOrNull { it.esHoy(reloj) }
            ?: return ventanilla(MotivoVentanilla.SIN_CITA_HOY, null)
        val solicitud = solicitudes.buscarPorCita(cita.id) ?: return ventanilla(MotivoVentanilla.SIN_RADICAR, cita)
        return when (val llegada = registrarLlegada(solicitud)) {
            is Llegada.Confirmada -> preconsulta(cita, llegada.solicitud.also(solicitudes::guardar))
            is Llegada.YaRegistrada -> preconsulta(cita, solicitud)
            is Llegada.AVentanilla -> ventanilla(llegada.motivo, cita, solicitud.novedades)
        }
    }

    private fun preconsulta(cita: Cita, solicitud: SolicitudPrefactura) =
        ResultadoDigiturno.Preconsulta(turnos.siguiente(DestinoTurno.PRECONSULTA), cita, solicitud)

    private fun ventanilla(motivo: MotivoVentanilla, cita: Cita?, novedades: List<Novedad> = emptyList()) =
        ResultadoDigiturno.Ventanilla(turnos.siguiente(DestinoTurno.FACTURACION), motivo, cita, novedades)
}
