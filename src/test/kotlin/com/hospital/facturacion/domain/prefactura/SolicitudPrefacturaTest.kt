package com.hospital.facturacion.domain.prefactura

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class SolicitudPrefacturaTest {
    private val aprobada = SolicitudPrefactura(ConsecutivoRadicado("PRUEBA-1"), EstadoPrefactura.APROBADO, Instant.EPOCH)

    @Test
    fun `aplica una transicion permitida`() {
        val cambio = aprobada.cambiarEstado(EstadoPrefactura.LISTO_PARA_FACTURAR)
        assertEquals(CambioEstado.Aplicado(aprobada.copy(estado = EstadoPrefactura.LISTO_PARA_FACTURAR)), cambio)
    }

    @Test
    fun `rechaza facturar sin que el digiturno confirme la llegada`() {
        val cambio = aprobada.cambiarEstado(EstadoPrefactura.FACTURADO)
        assertEquals(CambioEstado.Rechazado(EstadoPrefactura.APROBADO, EstadoPrefactura.FACTURADO), cambio)
    }
}
