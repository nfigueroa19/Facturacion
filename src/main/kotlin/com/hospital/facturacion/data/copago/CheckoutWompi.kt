package com.hospital.facturacion.data.copago

import com.hospital.facturacion.domain.cita.Cita
import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.cita.Copago
import com.hospital.facturacion.domain.copago.CheckoutPasarela
import com.hospital.facturacion.domain.copago.IntencionPago
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.HexFormat

/**
 * Parámetros firmados para el widget de Wompi. La referencia cambia en cada intento porque Wompi no admite repetirla.
 * Firma de integridad según Wompi: SHA-256 en hexadecimal de referencia, monto en centavos, moneda y secreto de
 * integridad, concatenados en ese orden.
 */
class CheckoutWompi(private val credenciales: CredencialesWompi, private val aleatorio: SecureRandom) : CheckoutPasarela {
    override fun preparar(cita: Cita, copago: Copago): IntencionPago {
        val referencia = prefijoReferencia(cita.id) + HexFormat.of().formatHex(ByteArray(6).also(aleatorio::nextBytes))
        val monto = copago.centavos()
        val firma = MessageDigest.getInstance("SHA-256").digest("$referencia$monto$MONEDA_WOMPI${credenciales.integridad}".toByteArray())
        return IntencionPago.Checkout(credenciales.publica, referencia, monto, MONEDA_WOMPI, HexFormat.of().formatHex(firma))
    }
}

/** Llaves de pruebas de Wompi. `base` cambia solo en pruebas; las llaves privada y de integridad no aparecen en `toString`. */
class CredencialesWompi(val publica: String, val privada: String, val integridad: String, val base: String = "https://sandbox.wompi.co") {
    override fun toString() = "CredencialesWompi($publica, ****, ****)"
}

/**
 * La referencia empieza por el ID de la cita para que `PasarelaWompi` compruebe que la transacción es de esa cita.
 * El guion bajo no cabe en un `CitaId`: `cita-1_` nunca es prefijo de la referencia de otra cita, como `cita-1-a`.
 */
internal fun prefijoReferencia(cita: CitaId) = "${cita.valor}_"

internal fun Copago.centavos() = valorPesos * 100

internal const val MONEDA_WOMPI = "COP"
