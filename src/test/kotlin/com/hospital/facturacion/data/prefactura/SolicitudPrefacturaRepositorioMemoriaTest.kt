package com.hospital.facturacion.data.prefactura

import com.hospital.facturacion.citaIdDePrueba
import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.prefactura.ConsecutivoRadicado
import com.hospital.facturacion.domain.prefactura.EstadoPrefactura
import com.hospital.facturacion.solicitudDePrueba
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SolicitudPrefacturaRepositorioMemoriaTest {
    @Test
    fun `guarda y recupera por consecutivo de radicado y por cita`() {
        val repositorio = SolicitudPrefacturaRepositorioMemoria()
        val solicitud = solicitudDePrueba(EstadoPrefactura.EN_REVISION)
        repositorio.guardar(solicitud)
        assertEquals(solicitud, repositorio.buscar(ConsecutivoRadicado("PF-1")))
        assertEquals(solicitud, repositorio.buscarPorCita(citaIdDePrueba))
        assertEquals(listOf(solicitud), repositorio.listar())
        assertNull(repositorio.buscar(ConsecutivoRadicado("PF-2")))
        assertNull(repositorio.buscarPorCita(requireNotNull(CitaId.de("otra"))))
    }

    @Test
    fun `el generador entrega consecutivos distintos y crecientes`() {
        val generador = GeneradorConsecutivoMemoria()
        assertEquals(listOf("PF-000001", "PF-000002"), List(2) { generador.siguiente().valor })
    }
}
