package com.hospital.facturacion.service.paciente

import com.hospital.facturacion.domain.cita.Cita
import com.hospital.facturacion.domain.copago.PagosCopago
import com.hospital.facturacion.domain.prefactura.SolicitudPrefactura
import com.hospital.facturacion.domain.prefactura.SolicitudPrefacturaRepositorio
import com.hospital.facturacion.domain.soporte.AlmacenSoportes
import com.hospital.facturacion.service.prefactura.Expediente
import com.hospital.facturacion.service.prefactura.expedienteDe

data class PrefacturaPaciente(val expediente: Expediente, val solicitud: SolicitudPrefactura?)

class ConsultarPrefacturaPaciente(
    private val solicitudes: SolicitudPrefacturaRepositorio,
    private val soportes: AlmacenSoportes,
    private val pagos: PagosCopago,
) {
    fun ejecutar(cita: Cita): PrefacturaPaciente =
        PrefacturaPaciente(expedienteDe(cita, soportes, pagos), solicitudes.buscarPorCita(cita.id))
}
