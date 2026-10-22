package com.hospital.facturacion.domain.prefactura

import com.hospital.facturacion.solicitudDePrueba
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CorreccionPrefacturaTest {
    private val novedad = requireNotNull(Novedad.de("  Falta la orden médica  ", "facturador", Instant.EPOCH))

    @Test
    fun `registrar una novedad la deja PENDIENTE y corregir la devuelve a EN_REVISION con los datos nuevos`() {
        val pendiente = (solicitudDePrueba(EstadoPrefactura.EN_REVISION).registrarNovedad(novedad) as CambioEstado.Aplicado).solicitud
        assertEquals(EstadoPrefactura.PENDIENTE to listOf(novedad), pendiente.estado to pendiente.novedades)
        val datos = DatosFacturacion(mapOf("nombre-padre" to "Corregido"))
        val corregida = (pendiente.corregir(datos, Instant.MAX) as CambioEstado.Aplicado).solicitud
        assertEquals(Triple(EstadoPrefactura.EN_REVISION, datos, Instant.MAX), Triple(corregida.estado, corregida.datos, corregida.fechaEnvio))
    }

    @Test
    fun `la novedad exige descripcion y la recorta`() {
        assertEquals("Falta la orden médica", novedad.descripcion)
        listOf("", "   ", "x".repeat(Novedad.LARGO_MAXIMO + 1)).forEach { assertEquals(null, Novedad.de(it, "f", Instant.EPOCH)) }
    }

    @Test
    fun `el paciente solo cambia su informacion antes de radicar o con novedades`() {
        assertTrue(null.admiteCambiosDelPaciente())
        assertTrue(solicitudDePrueba(EstadoPrefactura.PENDIENTE).admiteCambiosDelPaciente())
        (EstadoPrefactura.entries - EstadoPrefactura.PENDIENTE).forEach {
            assertFalse(solicitudDePrueba(it).admiteCambiosDelPaciente(), it.name)
        }
    }
}
