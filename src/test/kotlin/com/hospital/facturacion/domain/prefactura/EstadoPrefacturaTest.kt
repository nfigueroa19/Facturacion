package com.hospital.facturacion.domain.prefactura

import com.hospital.facturacion.domain.prefactura.EstadoPrefactura.APROBADO
import com.hospital.facturacion.domain.prefactura.EstadoPrefactura.FACTURADO
import com.hospital.facturacion.domain.prefactura.EstadoPrefactura.INGRESO_ANULADO
import com.hospital.facturacion.domain.prefactura.EstadoPrefactura.LISTO_PARA_FACTURAR
import com.hospital.facturacion.domain.prefactura.EstadoPrefactura.PENDIENTE
import com.hospital.facturacion.domain.prefactura.EstadoPrefactura.RADICADO
import kotlin.test.Test
import kotlin.test.assertEquals

class EstadoPrefacturaTest {
    private val diagramaFacturador = mapOf(
        RADICADO to setOf(PENDIENTE, APROBADO),
        PENDIENTE to setOf(RADICADO),
        APROBADO to setOf(INGRESO_ANULADO, LISTO_PARA_FACTURAR),
        LISTO_PARA_FACTURAR to setOf(FACTURADO),
    )

    @Test
    fun `solo permite las transiciones del diagrama del facturador`() {
        for (origen in EstadoPrefactura.entries) {
            for (destino in EstadoPrefactura.entries) {
                val esperado = destino in diagramaFacturador[origen].orEmpty()
                assertEquals(esperado, origen.puedePasarA(destino), "$origen -> $destino")
            }
        }
    }
}
