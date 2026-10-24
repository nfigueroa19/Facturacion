package com.hospital.facturacion.service.paciente

import com.hospital.facturacion.domain.cita.Cita
import com.hospital.facturacion.domain.comunicacion.MotivoComunicacion
import com.hospital.facturacion.domain.comunicacion.NotificadorPaciente
import com.hospital.facturacion.domain.formulario.ErrorCampo
import com.hospital.facturacion.domain.formulario.FormularioPrefactura
import com.hospital.facturacion.domain.formulario.ResultadoDatos
import com.hospital.facturacion.domain.prefactura.CambioEstado
import com.hospital.facturacion.domain.prefactura.EstadoPrefactura
import com.hospital.facturacion.domain.prefactura.GeneradorConsecutivo
import com.hospital.facturacion.domain.prefactura.SolicitudPrefactura
import com.hospital.facturacion.domain.prefactura.SolicitudPrefacturaRepositorio
import com.hospital.facturacion.domain.prefactura.admiteCambiosDelPaciente
import com.hospital.facturacion.domain.soporte.AlmacenSoportes
import com.hospital.facturacion.domain.soporte.TipoSoporte
import java.time.Clock

sealed interface ResultadoRadicacion {
    data class Radicada(val solicitud: SolicitudPrefactura) : ResultadoRadicacion

    data class DatosInvalidos(val errores: Map<String, ErrorCampo>) : ResultadoRadicacion

    data class FaltanSoportes(val tipos: Set<TipoSoporte>) : ResultadoRadicacion

    data object NoEditable : ResultadoRadicacion
}

/**
 * Primer envío o reenvío tras corregir novedades. En ambos casos la solicitud queda EN_REVISION y el paciente recibe
 * el consecutivo por SMS.
 */
class RadicarSolicitud(
    private val formulario: FormularioPrefactura,
    private val solicitudes: SolicitudPrefacturaRepositorio,
    private val soportes: AlmacenSoportes,
    private val consecutivos: GeneradorConsecutivo,
    private val notificador: NotificadorPaciente,
    private val reloj: Clock,
) {
    fun ejecutar(cita: Cita, valores: Map<String, String>): ResultadoRadicacion {
        val actual = solicitudes.buscarPorCita(cita.id)
        if (!actual.admiteCambiosDelPaciente()) return ResultadoRadicacion.NoEditable
        val datos = when (val resultado = formulario.validar(valores)) {
            is ResultadoDatos.Invalidos -> return ResultadoRadicacion.DatosInvalidos(resultado.errores)
            is ResultadoDatos.Validos -> resultado.datos
        }
        val faltantes = TipoSoporte.entries.toSet() - soportes.cargados(cita.id)
        if (faltantes.isNotEmpty()) return ResultadoRadicacion.FaltanSoportes(faltantes)
        val radicada = when (val cambio = actual?.corregir(datos, reloj.instant())) {
            null -> SolicitudPrefactura(consecutivos.siguiente(), cita.id, EstadoPrefactura.EN_REVISION, reloj.instant(), datos)
            is CambioEstado.Aplicado -> cambio.solicitud
            is CambioEstado.Rechazado, is CambioEstado.Incumple -> return ResultadoRadicacion.NoEditable
        }
        solicitudes.guardar(radicada)
        notificador.notificar(cita, MotivoComunicacion.SOLICITUD_RADICADA, mapOf("consecutivo" to radicada.consecutivo.valor))
        return ResultadoRadicacion.Radicada(radicada)
    }
}
