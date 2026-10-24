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
 * Parámetros para que el navegador cree la transacción en Wompi. La referencia cambia en cada intento porque Wompi no
 * admite repetirla. Firma de integridad según Wompi: SHA-256 en hexadecimal de referencia, monto en centavos, moneda y
 * secreto de integridad, concatenados en ese orden. Sin secreto no hay firma: el comercio de demostración no la exige.
 */
class CheckoutWompi(private val credenciales: CredencialesWompi, private val contratos: ContratosWompi, private val aleatorio: SecureRandom) :
    CheckoutPasarela {
    override fun preparar(cita: Cita, copago: Copago): IntencionPago? {
        val aceptacion = contratos.consultar() ?: return null
        val referencia = prefijoReferencia(cita.id) + HexFormat.of().formatHex(ByteArray(6).also(aleatorio::nextBytes))
        val monto = copago.centavos()
        val firma = credenciales.integridad?.let { sha256("$referencia$monto$MONEDA_WOMPI$it") }
        return IntencionPago(credenciales.publica, referencia, monto, MONEDA_WOMPI, firma, aceptacion)
    }
}

/** Llaves de pruebas de Wompi. `base` sale de la configuración; el secreto de integridad no aparece en `toString`. */
class CredencialesWompi(val publica: String, val integridad: String?, val base: String) {
    override fun toString() = "CredencialesWompi($publica, ${if (integridad == null) "sin integridad" else "****"})"
}

/**
 * La referencia empieza por el ID de la cita para que `PasarelaWompi` compruebe que la transacción es de esa cita.
 * El guion bajo no cabe en un `CitaId`: `cita-1_` nunca es prefijo de la referencia de otra cita, como `cita-1-a`.
 */
internal fun prefijoReferencia(cita: CitaId) = "${cita.valor}_"

internal fun Copago.centavos() = valorPesos * 100

internal const val MONEDA_WOMPI = "COP"

private fun sha256(texto: String) = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(texto.toByteArray()))
