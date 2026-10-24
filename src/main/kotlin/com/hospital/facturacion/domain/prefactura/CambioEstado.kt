package com.hospital.facturacion.domain.prefactura

sealed interface CambioEstado {
    data class Aplicado(val solicitud: SolicitudPrefactura) : CambioEstado

    data class Rechazado(val actual: EstadoPrefactura, val destino: EstadoPrefactura) : CambioEstado

    data class Incumple(val motivo: MotivoRechazoAtencion) : CambioEstado
}
