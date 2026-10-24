package com.hospital.facturacion.domain.copago

import com.hospital.facturacion.domain.cita.Cita
import com.hospital.facturacion.domain.cita.Copago

/**
 * Lo que el navegador necesita para crear la transacción en la pasarela antes de enviarle al backend su ID. `firma` la
 * calcula el backend con un secreto que nunca sale de él; es null si el comercio configurado no la exige.
 */
data class IntencionPago(
    val llavePublica: String,
    val referencia: String,
    val montoCentavos: Long,
    val moneda: String,
    val firma: String?,
    val aceptacion: AceptacionPasarela,
)

/** Contratos que el paciente acepta antes de pagar. Cada token sirve para una sola transacción. */
data class AceptacionPasarela(val terminos: ContratoPasarela, val datosPersonales: ContratoPasarela)

data class ContratoPasarela(val token: String, val enlace: String)

/** Prepara el cobro del copago en la pasarela. Cada llamada produce una referencia y tokens de aceptación nuevos. */
fun interface CheckoutPasarela {
    /** Null si la pasarela no entregó los contratos de aceptación. */
    fun preparar(cita: Cita, copago: Copago): IntencionPago?
}
