package com.hospital.facturacion.domain.prefactura

import com.hospital.facturacion.solicitudDePrueba
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class SolicitudPrefacturaTest {
    private val aprobada = solicitudDePrueba(EstadoPrefactura.APROBADO)
    private val enRevision = solicitudDePrueba(EstadoPrefactura.EN_REVISION)

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

    @Test
    fun `aprobar guarda la validacion y no aplica sobre una solicitud aprobada`() {
        val validacion = Validacion("facturador", Instant.EPOCH)
        assertEquals(CambioEstado.Aplicado(aprobada.copy(validacion = validacion)), enRevision.aprobar(validacion))
        assertEquals(CambioEstado.Rechazado(EstadoPrefactura.APROBADO, EstadoPrefactura.APROBADO), aprobada.aprobar(validacion))
    }
}
