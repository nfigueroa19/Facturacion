package com.hospital.facturacion.domain.copago

import com.hospital.facturacion.domain.cita.Cita
import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.cita.Copago
import java.time.Instant

data class PagoCopago(val referencia: String, val copago: Copago, val fecha: Instant)

/**
 * Lo que el navegador entrega después de cobrar: el ID de la transacción que creó el checkout de la pasarela o, con la
 * pasarela de pruebas, el token de la tarjeta de prueba. El número de la tarjeta nunca llega al backend: así funcionan
 * Wompi y las demás pasarelas evaluadas, y así se mantiene el alcance PCI mínimo.
 */
@JvmInline
value class TokenPago private constructor(val valor: String) {
    companion object {
        private val PATRON = Regex("[A-Za-z0-9_-]{1,100}")

        fun de(valor: String): TokenPago? = valor.takeIf(PATRON::matches)?.let(::TokenPago)
    }
}

enum class MotivoRechazoPago { RECHAZADA, FONDOS_INSUFICIENTES, ERROR_PASARELA, MEDIO_INVALIDO }

sealed interface ResultadoPasarela {
    data class Aprobado(val referencia: String) : ResultadoPasarela

    data class Rechazado(val motivo: MotivoRechazoPago) : ResultadoPasarela
}

/** Verifica o ejecuta el cobro en la pasarela del hospital. El proveedor y el convenio siguen abiertos (P-14). */
fun interface PasarelaPagos {
    fun cobrar(cita: Cita, copago: Copago, medio: TokenPago): ResultadoPasarela
}

interface PagosCopago {
    fun guardar(cita: CitaId, pago: PagoCopago)

    fun buscar(cita: CitaId): PagoCopago?
}
