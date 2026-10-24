package com.hospital.facturacion.data.copago

import com.hospital.facturacion.domain.cita.Cita
import com.hospital.facturacion.domain.cita.Copago
import com.hospital.facturacion.domain.copago.CheckoutPasarela
import com.hospital.facturacion.domain.copago.IntencionPago
import com.hospital.facturacion.domain.copago.MotivoRechazoPago
import com.hospital.facturacion.domain.copago.PasarelaPagos
import com.hospital.facturacion.domain.copago.ResultadoPasarela
import com.hospital.facturacion.domain.copago.TokenPago
import java.security.SecureRandom
import java.util.HexFormat

/**
 * Pasarela de pruebas sin dinero. Cada token de prueba produce un resultado fijo, como las tarjetas de prueba del
 * sandbox de Wompi (4242... aprobada, 4111... rechazada). Se usa mientras el almacén no tenga las llaves de Wompi.
 */
class PasarelaPagosSimulada(private val aleatorio: SecureRandom) : PasarelaPagos, CheckoutPasarela {
    override fun preparar(cita: Cita, copago: Copago) = IntencionPago.Simulada

    override fun cobrar(cita: Cita, copago: Copago, medio: TokenPago): ResultadoPasarela = when (medio.valor) {
        TOKEN_APROBADA -> ResultadoPasarela.Aprobado("SIM-" + HexFormat.of().withUpperCase().formatHex(ByteArray(4).also(aleatorio::nextBytes)))
        TOKEN_RECHAZADA -> ResultadoPasarela.Rechazado(MotivoRechazoPago.RECHAZADA)
        TOKEN_FONDOS_INSUFICIENTES -> ResultadoPasarela.Rechazado(MotivoRechazoPago.FONDOS_INSUFICIENTES)
        TOKEN_ERROR -> ResultadoPasarela.Rechazado(MotivoRechazoPago.ERROR_PASARELA)
        else -> ResultadoPasarela.Rechazado(MotivoRechazoPago.MEDIO_INVALIDO)
    }

    companion object {
        const val TOKEN_APROBADA = "tok_prueba_aprobada"
        const val TOKEN_RECHAZADA = "tok_prueba_rechazada"
        const val TOKEN_FONDOS_INSUFICIENTES = "tok_prueba_fondos_insuficientes"
        const val TOKEN_ERROR = "tok_prueba_error"
    }
}
