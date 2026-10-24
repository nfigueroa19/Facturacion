package com.hospital.facturacion.domain.copago

import com.hospital.facturacion.domain.cita.Cita
import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.cita.Copago
import java.time.Instant

data class PagoCopago(val referencia: String, val copago: Copago, val fecha: Instant)

/**
 * Lo que el navegador entrega después de cobrar: el ID de la transacción que creó en la pasarela. El número de la
 * tarjeta nunca llega al backend: el navegador la tokeniza directamente en Wompi, y así se mantiene el alcance PCI mínimo.
 */
@JvmInline
value class TokenPago private constructor(val valor: String) {
    companion object {
        private val PATRON = Regex("[A-Za-z0-9_-]{1,100}")

        fun de(valor: String): TokenPago? = valor.takeIf(PATRON::matches)?.let(::TokenPago)
    }
}

enum class MotivoRechazoPago { RECHAZADA, ERROR_PASARELA, MEDIO_INVALIDO }

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
