package com.hospital.facturacion.domain.copago

import com.hospital.facturacion.domain.cita.Cita
import com.hospital.facturacion.domain.cita.Copago

/** Lo que el navegador necesita para cobrar el copago antes de enviarle al backend el resultado. */
sealed interface IntencionPago {
    /** Pasarela de pruebas interna: el navegador elige una tarjeta de prueba y envía su token. */
    data object Simulada : IntencionPago

    /**
     * Checkout web de la pasarela. El backend calcula `firma` con un secreto que nunca sale de él: la pasarela rechaza
     * el cobro si alguien cambia la referencia o el monto en el navegador.
     */
    data class Checkout(
        val llavePublica: String,
        val referencia: String,
        val montoCentavos: Long,
        val moneda: String,
        val firma: String,
    ) : IntencionPago
}

/** Prepara el cobro del copago en la pasarela. Cada llamada produce una referencia nueva. */
fun interface CheckoutPasarela {
    fun preparar(cita: Cita, copago: Copago): IntencionPago
}
