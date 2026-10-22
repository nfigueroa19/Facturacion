package com.hospital.facturacion.service.facturador

import com.hospital.facturacion.domain.cita.AgendaCitas
import com.hospital.facturacion.domain.copago.PagosCopago
import com.hospital.facturacion.domain.prefactura.SolicitudPrefactura
import com.hospital.facturacion.domain.soporte.AlmacenSoportes
import com.hospital.facturacion.service.prefactura.Expediente
import com.hospital.facturacion.service.prefactura.expedienteDe

/** Solicitud con todo lo que el facturador necesita para revisarla. */
data class SolicitudRevisable(val solicitud: SolicitudPrefactura, val expediente: Expediente)

fun revisable(
    solicitud: SolicitudPrefactura,
    agenda: AgendaCitas,
    soportes: AlmacenSoportes,
    pagos: PagosCopago,
): SolicitudRevisable? = agenda.buscar(solicitud.cita)?.let { SolicitudRevisable(solicitud, expedienteDe(it, soportes, pagos)) }
