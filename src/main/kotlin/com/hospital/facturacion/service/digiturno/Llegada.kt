package com.hospital.facturacion.service.digiturno

import com.hospital.facturacion.domain.digiturno.MotivoVentanilla
import com.hospital.facturacion.domain.prefactura.CambioEstado
import com.hospital.facturacion.domain.prefactura.EstadoPrefactura
import com.hospital.facturacion.domain.prefactura.SolicitudPrefactura

sealed interface Llegada {
    data class Confirmada(val solicitud: SolicitudPrefactura) : Llegada

    /** Leyó la identificación por segunda vez: la llegada ya estaba registrada. */
    data object YaRegistrada : Llegada

    data class AVentanilla(val motivo: MotivoVentanilla) : Llegada
}

/** Decide la ruta del paciente según el estado de su solicitud (diagrama del paciente, nodo T). */
fun registrarLlegada(solicitud: SolicitudPrefactura): Llegada = when (solicitud.estado) {
    EstadoPrefactura.APROBADO -> when (val cambio = solicitud.cambiarEstado(EstadoPrefactura.LISTO_PARA_FACTURAR)) {
        is CambioEstado.Aplicado -> Llegada.Confirmada(cambio.solicitud)
        is CambioEstado.Rechazado, is CambioEstado.Incumple -> Llegada.AVentanilla(MotivoVentanilla.EN_REVISION)
    }
    EstadoPrefactura.LISTO_PARA_FACTURAR, EstadoPrefactura.FACTURADO -> Llegada.YaRegistrada
    EstadoPrefactura.EN_REVISION -> Llegada.AVentanilla(MotivoVentanilla.EN_REVISION)
    EstadoPrefactura.PENDIENTE -> Llegada.AVentanilla(MotivoVentanilla.CON_NOVEDADES)
    EstadoPrefactura.INGRESO_ANULADO -> Llegada.AVentanilla(MotivoVentanilla.INGRESO_ANULADO)
}
