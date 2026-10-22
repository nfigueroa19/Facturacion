package com.hospital.facturacion.service.facturador

import com.hospital.facturacion.domain.cita.AgendaCitas
import com.hospital.facturacion.domain.copago.PagosCopago
import com.hospital.facturacion.domain.prefactura.ConsecutivoRadicado
import com.hospital.facturacion.domain.prefactura.SolicitudPrefacturaRepositorio
import com.hospital.facturacion.domain.soporte.AlmacenSoportes

class ConsultarSolicitud(
    private val solicitudes: SolicitudPrefacturaRepositorio,
    private val agenda: AgendaCitas,
    private val soportes: AlmacenSoportes,
    private val pagos: PagosCopago,
) {
    fun ejecutar(consecutivo: ConsecutivoRadicado): SolicitudRevisable? =
        solicitudes.buscar(consecutivo)?.let { revisable(it, agenda, soportes, pagos) }
}
