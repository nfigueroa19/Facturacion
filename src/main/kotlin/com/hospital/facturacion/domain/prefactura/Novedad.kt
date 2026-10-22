package com.hospital.facturacion.domain.prefactura

import java.time.Instant

/** Dato o documento faltante o errado que registra el facturador. */
data class Novedad private constructor(val descripcion: String, val facturador: String, val fecha: Instant) {
    companion object {
        const val LARGO_MAXIMO = 1000

        fun de(descripcion: String, facturador: String, fecha: Instant): Novedad? =
            descripcion.trim().takeIf { it.isNotEmpty() && it.length <= LARGO_MAXIMO }
                ?.let { Novedad(it, facturador, fecha) }
    }
}
