package com.hospital.facturacion.domain.prefactura

import java.time.Instant

data class SolicitudPrefactura(
    val consecutivo: ConsecutivoRadicado,
    val estado: EstadoPrefactura,
    val fechaEnvio: Instant,
) {
    fun cambiarEstado(destino: EstadoPrefactura): CambioEstado =
        if (estado.puedePasarA(destino)) {
            CambioEstado.Aplicado(copy(estado = destino))
        } else {
            CambioEstado.Rechazado(actual = estado, destino = destino)
        }
}
