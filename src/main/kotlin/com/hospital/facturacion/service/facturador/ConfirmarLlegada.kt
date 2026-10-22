package com.hospital.facturacion.service.facturador

import com.hospital.facturacion.domain.prefactura.ConsecutivoRadicado
import com.hospital.facturacion.domain.prefactura.EstadoPrefactura
import com.hospital.facturacion.domain.prefactura.SolicitudPrefacturaRepositorio

/**
 * El digiturno confirma la llegada y la solicitud pasa a LISTO_PARA_FACTURAR (RN-14). Mientras no exista la
 * integración con el digiturno (P-15), el facturador registra la llegada desde el panel.
 */
class ConfirmarLlegada(private val solicitudes: SolicitudPrefacturaRepositorio) {
    fun ejecutar(consecutivo: ConsecutivoRadicado): ResultadoAccion {
        val solicitud = solicitudes.buscar(consecutivo) ?: return ResultadoAccion.NoEncontrada
        return solicitudes.aplicar(solicitud.cambiarEstado(EstadoPrefactura.LISTO_PARA_FACTURAR))
    }
}
