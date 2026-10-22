package com.hospital.facturacion.service.facturador

import com.hospital.facturacion.domain.cita.AgendaCitas
import com.hospital.facturacion.domain.copago.PagosCopago
import com.hospital.facturacion.domain.prefactura.EstadoPrefactura
import com.hospital.facturacion.domain.prefactura.SolicitudPrefacturaRepositorio
import com.hospital.facturacion.domain.soporte.AlmacenSoportes

/** Cola del facturador, de la más antigua a la más reciente por fecha de envío. */
class ListarSolicitudes(
    private val solicitudes: SolicitudPrefacturaRepositorio,
    private val agenda: AgendaCitas,
    private val soportes: AlmacenSoportes,
    private val pagos: PagosCopago,
) {
    fun ejecutar(estado: EstadoPrefactura?): List<SolicitudRevisable> = solicitudes.listar()
        .filter { estado == null || it.estado == estado }
        .sortedBy { it.fechaEnvio }
        .mapNotNull { revisable(it, agenda, soportes, pagos) }
}
