package com.hospital.facturacion.domain.prefactura

import com.hospital.facturacion.solicitudDePrueba
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class AnulacionYValoresTest {
    private val fecha = Instant.parse("2026-10-22T13:00:00Z")
    private val ingreso = Registro(requireNotNull(NumeroDinamica.de("ING-1")), "facturador", fecha)

    private fun CambioEstado.aplicado(): SolicitudPrefactura = assertIs<CambioEstado.Aplicado>(this).solicitud

    @Test
    fun `anula solo un ingreso registrado de una solicitud aprobada`() {
        val motivo = Registro(requireNotNull(Observacion.de("No llegó a la cita")), "facturador", fecha)
        val aprobada = solicitudDePrueba(EstadoPrefactura.APROBADO)
        assertEquals(CambioEstado.Incumple(MotivoRechazoAtencion.SIN_INGRESO), aprobada.anularIngreso(motivo))
        val anulada = aprobada.registrarIngreso(ingreso).aplicado().anularIngreso(motivo).aplicado()
        assertEquals(EstadoPrefactura.INGRESO_ANULADO to motivo, anulada.estado to anulada.atencion.anulacion)
        val lista = solicitudDePrueba(EstadoPrefactura.LISTO_PARA_FACTURAR)
        assertEquals(CambioEstado.Rechazado(EstadoPrefactura.LISTO_PARA_FACTURAR, EstadoPrefactura.INGRESO_ANULADO), lista.anularIngreso(motivo))
    }

    @Test
    fun `los numeros de Dinamica y las observaciones rechazan valores vacios o con formato no admitido`() {
        assertEquals("ING-1", NumeroDinamica.de("  ING-1 ")?.valor)
        assertNull(NumeroDinamica.de(""))
        assertNull(NumeroDinamica.de("ING 1"))
        assertNull(NumeroDinamica.de("x".repeat(31)))
        assertNull(Observacion.de("   "))
        assertNull(Observacion.de("x".repeat(Novedad.LARGO_MAXIMO + 1)))
    }
}
