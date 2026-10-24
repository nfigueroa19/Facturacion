package com.hospital.facturacion.data.pruebas

import com.hospital.facturacion.domain.prefactura.EstadoPrefactura
import com.hospital.facturacion.solicitudDePrueba
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EtapaEscenarioTest {
    private val recienRadicada = solicitudDePrueba(EstadoPrefactura.EN_REVISION)
    private val fecha = Instant.parse("2026-10-24T14:00:00Z")

    private fun EtapaEscenario.solicitud() = aplicar(recienRadicada, fecha)

    @Test
    fun `cada etapa llega a su estado por las transiciones del dominio`() {
        val esperados = mapOf(
            EtapaEscenario.EN_REVISION to EstadoPrefactura.EN_REVISION,
            EtapaEscenario.PENDIENTE to EstadoPrefactura.PENDIENTE,
            EtapaEscenario.APROBADO to EstadoPrefactura.APROBADO,
            EtapaEscenario.APROBADO_CON_INGRESO to EstadoPrefactura.APROBADO,
            EtapaEscenario.LISTO_PARA_FACTURAR to EstadoPrefactura.LISTO_PARA_FACTURAR,
            EtapaEscenario.FACTURADO to EstadoPrefactura.FACTURADO,
            EtapaEscenario.INGRESO_ANULADO to EstadoPrefactura.INGRESO_ANULADO,
        )
        assertEquals(esperados, esperados.keys.associateWith { it.solicitud().estado })
        assertFalse(EtapaEscenario.SIN_RADICAR.radicada)
        assertTrue(esperados.keys.all { it.radicada })
    }

    @Test
    fun `las etapas guardan la novedad, la validacion, el ingreso, la factura y la anulacion`() {
        assertEquals(1, EtapaEscenario.PENDIENTE.solicitud().novedades.size)
        assertNull(EtapaEscenario.APROBADO.solicitud().atencion.ingreso)
        assertEquals("facturador", EtapaEscenario.APROBADO.solicitud().validacion?.facturador)
        assertNotNull(EtapaEscenario.LISTO_PARA_FACTURAR.solicitud().atencion.ingreso)
        assertNotNull(EtapaEscenario.FACTURADO.solicitud().atencion.factura)
        assertNotNull(EtapaEscenario.INGRESO_ANULADO.solicitud().atencion.anulacion)
    }
}
