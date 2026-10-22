package com.hospital.facturacion.domain.prefactura

/** Consecutivo que recibe el paciente al enviar la solicitud. El formato está pendiente de definir. */
@JvmInline
value class ConsecutivoRadicado(val valor: String) {
    init {
        require(valor.isNotBlank()) { "El consecutivo de radicado no puede estar vacío" }
    }
}
