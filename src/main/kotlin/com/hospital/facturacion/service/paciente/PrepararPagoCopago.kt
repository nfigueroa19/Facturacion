package com.hospital.facturacion.service.paciente

import com.hospital.facturacion.domain.cita.Cita
import com.hospital.facturacion.domain.copago.CheckoutPasarela
import com.hospital.facturacion.domain.copago.PagosCopago
import com.hospital.facturacion.domain.prefactura.SolicitudPrefacturaRepositorio

/** Antes de abrir la pasarela en el navegador: verifica que el copago se pueda pagar en la web y prepara el cobro. */
class PrepararPagoCopago(
    private val solicitudes: SolicitudPrefacturaRepositorio,
    private val pagos: PagosCopago,
    private val checkout: CheckoutPasarela,
) {
    fun ejecutar(cita: Cita): ResultadoPreparacion {
        val copago = cita.copago ?: return ResultadoPago.SinCopago
        impedimentoDeCobro(cita, solicitudes, pagos)?.let { return it }
        return checkout.preparar(cita, copago)?.let(ResultadoPreparacion::Lista) ?: ResultadoPreparacion.PasarelaNoDisponible
    }
}
