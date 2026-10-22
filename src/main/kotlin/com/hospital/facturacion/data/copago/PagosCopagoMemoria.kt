package com.hospital.facturacion.data.copago

import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.copago.PagoCopago
import com.hospital.facturacion.domain.copago.PagosCopago
import java.util.concurrent.ConcurrentHashMap

class PagosCopagoMemoria : PagosCopago {
    private val pagos = ConcurrentHashMap<CitaId, PagoCopago>()

    override fun guardar(cita: CitaId, pago: PagoCopago) {
        pagos[cita] = pago
    }

    override fun buscar(cita: CitaId): PagoCopago? = pagos[cita]
}
