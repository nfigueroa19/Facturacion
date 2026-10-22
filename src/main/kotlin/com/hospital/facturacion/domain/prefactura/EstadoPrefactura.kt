package com.hospital.facturacion.domain.prefactura

/**
 * Estados del diagrama del facturador (reunión del 2026-10-21).
 * RADICADO es el nombre provisional del estado inicial mientras se responde la pregunta P-04.
 */
enum class EstadoPrefactura {
    RADICADO,
    PENDIENTE,
    APROBADO,
    INGRESO_ANULADO,
    LISTO_PARA_FACTURAR,
    FACTURADO;

    fun puedePasarA(destino: EstadoPrefactura): Boolean = destino in TRANSICIONES.getValue(this)

    private companion object {
        val TRANSICIONES = mapOf(
            RADICADO to setOf(PENDIENTE, APROBADO),
            PENDIENTE to setOf(RADICADO),
            APROBADO to setOf(INGRESO_ANULADO, LISTO_PARA_FACTURAR),
            LISTO_PARA_FACTURAR to setOf(FACTURADO),
            INGRESO_ANULADO to emptySet(),
            FACTURADO to emptySet(),
        )
    }
}
