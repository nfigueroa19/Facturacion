package com.hospital.facturacion.data.prefactura

import com.hospital.facturacion.domain.prefactura.ConsecutivoRadicado
import com.hospital.facturacion.domain.prefactura.EstadoPrefactura
import com.hospital.facturacion.domain.prefactura.SolicitudPrefactura
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SolicitudPrefacturaRepositorioMemoriaTest {
    @Test
    fun `guarda y recupera por consecutivo de radicado`() {
        val repositorio = SolicitudPrefacturaRepositorioMemoria()
        val solicitud = SolicitudPrefactura(ConsecutivoRadicado("PRUEBA-1"), EstadoPrefactura.RADICADO, Instant.EPOCH)
        repositorio.guardar(solicitud)
        assertEquals(solicitud, repositorio.buscar(ConsecutivoRadicado("PRUEBA-1")))
        assertNull(repositorio.buscar(ConsecutivoRadicado("PRUEBA-2")))
    }
}
