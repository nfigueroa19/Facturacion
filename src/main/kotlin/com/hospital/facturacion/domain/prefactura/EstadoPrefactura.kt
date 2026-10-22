package com.hospital.facturacion.domain.prefactura

/**
 * Estados del diagrama del facturador (reunión del 2026-10-21).
 * EN_REVISION es el estado entre el envío del paciente y la revisión del facturador (P-04, confirmado 2026-10-22).
 */
enum class EstadoPrefactura {
    EN_REVISION,
    PENDIENTE,
    APROBADO,
    INGRESO_ANULADO,
    LISTO_PARA_FACTURAR,
    FACTURADO;

    fun puedePasarA(destino: EstadoPrefactura): Boolean = destino in TRANSICIONES.getValue(this)

    private companion object {
        val TRANSICIONES = mapOf(
            EN_REVISION to setOf(PENDIENTE, APROBADO),
            PENDIENTE to setOf(EN_REVISION),
            APROBADO to setOf(INGRESO_ANULADO, LISTO_PARA_FACTURAR),
            LISTO_PARA_FACTURAR to setOf(FACTURADO),
            INGRESO_ANULADO to emptySet(),
            FACTURADO to emptySet(),
        )
    }
}
