package com.hospital.facturacion.service.facturador

import com.hospital.facturacion.domain.cita.AgendaCitas
import com.hospital.facturacion.domain.comunicacion.MotivoComunicacion
import com.hospital.facturacion.domain.comunicacion.NotificadorPaciente
import com.hospital.facturacion.domain.facturador.Facturador
import com.hospital.facturacion.domain.prefactura.ConsecutivoRadicado
import com.hospital.facturacion.domain.prefactura.Novedad
import com.hospital.facturacion.domain.prefactura.SolicitudPrefacturaRepositorio
import java.time.Clock

/**
 * La solicitud queda PENDIENTE y se avisa al paciente por SMS (RN-12). El SMS no lleva el texto de la novedad:
 * el paciente la lee al ingresar. El digiturno la muestra al leer la identificación del paciente.
 */
class RegistrarNovedad(
    private val solicitudes: SolicitudPrefacturaRepositorio,
    private val agenda: AgendaCitas,
    private val notificador: NotificadorPaciente,
    private val reloj: Clock,
) {
    fun ejecutar(consecutivo: ConsecutivoRadicado, descripcion: String, facturador: Facturador): ResultadoAccion {
        val solicitud = solicitudes.buscar(consecutivo) ?: return ResultadoAccion.NoEncontrada
        val novedad = Novedad.de(descripcion, facturador.usuario, reloj.instant()) ?: return ResultadoAccion.NovedadInvalida
        val resultado = solicitudes.aplicar(solicitud.registrarNovedad(novedad))
        val cita = agenda.buscar(solicitud.cita)
        if (resultado is ResultadoAccion.Aplicada && cita != null) {
            notificador.notificar(cita, MotivoComunicacion.NOVEDAD_REGISTRADA, mapOf("consecutivo" to consecutivo.valor))
        }
        return resultado
    }
}
