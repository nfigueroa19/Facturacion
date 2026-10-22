package com.hospital.facturacion.service.prefactura

import com.hospital.facturacion.domain.cita.Cita
import com.hospital.facturacion.domain.copago.PagoCopago
import com.hospital.facturacion.domain.copago.PagosCopago
import com.hospital.facturacion.domain.soporte.AlmacenSoportes
import com.hospital.facturacion.domain.soporte.TipoSoporte

/** Lo que el paciente adelantó para una cita, aparte de la solicitud: soportes cargados y pago del copago. */
data class Expediente(val cita: Cita, val soportes: Set<TipoSoporte>, val pago: PagoCopago?)

fun expedienteDe(cita: Cita, soportes: AlmacenSoportes, pagos: PagosCopago): Expediente =
    Expediente(cita, soportes.cargados(cita.id), pagos.buscar(cita.id))
