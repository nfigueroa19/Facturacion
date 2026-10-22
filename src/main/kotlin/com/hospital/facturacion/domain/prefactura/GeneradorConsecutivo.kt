package com.hospital.facturacion.domain.prefactura

fun interface GeneradorConsecutivo {
    fun siguiente(): ConsecutivoRadicado
}
