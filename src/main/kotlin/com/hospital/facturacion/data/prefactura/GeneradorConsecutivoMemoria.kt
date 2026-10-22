package com.hospital.facturacion.data.prefactura

import com.hospital.facturacion.domain.prefactura.ConsecutivoRadicado
import com.hospital.facturacion.domain.prefactura.GeneradorConsecutivo
import java.util.concurrent.atomic.AtomicLong

/** Formato provisional `PF-000001`: el formato real del consecutivo de radicado no está definido. */
class GeneradorConsecutivoMemoria : GeneradorConsecutivo {
    private val ultimo = AtomicLong()

    override fun siguiente(): ConsecutivoRadicado = ConsecutivoRadicado("PF-%06d".format(ultimo.incrementAndGet()))
}
