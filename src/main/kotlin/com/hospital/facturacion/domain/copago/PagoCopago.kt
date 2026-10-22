package com.hospital.facturacion.domain.copago

import com.hospital.facturacion.domain.cita.Cita
import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.cita.Copago
import java.time.Instant

data class PagoCopago(val referencia: String, val copago: Copago, val fecha: Instant)

sealed interface ResultadoPasarela {
    data class Aprobado(val referencia: String) : ResultadoPasarela

    data class Rechazado(val motivo: String) : ResultadoPasarela
}

/** Pasarela de pagos del hospital. El proveedor y el convenio siguen abiertos (P-14). */
fun interface PasarelaPagos {
    fun cobrar(cita: Cita, copago: Copago): ResultadoPasarela
}

interface PagosCopago {
    fun guardar(cita: CitaId, pago: PagoCopago)

    fun buscar(cita: CitaId): PagoCopago?
}
