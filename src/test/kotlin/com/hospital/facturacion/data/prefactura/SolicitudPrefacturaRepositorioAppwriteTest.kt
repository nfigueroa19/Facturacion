package com.hospital.facturacion.data.prefactura

import com.hospital.facturacion.AppwriteDePrueba
import com.hospital.facturacion.domain.prefactura.EstadoPrefactura
import com.hospital.facturacion.solicitudDePrueba
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterEach
import kotlin.test.Test
import kotlin.test.assertEquals

class SolicitudPrefacturaRepositorioAppwriteTest : RepositorioSolicitudesContrato() {
    private val appwrite = AppwriteDePrueba()
    private val novedades = appwrite.tabla("novedades", FilaNovedad.serializer())
    override val repositorio = SolicitudPrefacturaRepositorioAppwrite(appwrite.tabla("solicitudes", FilaSolicitud.serializer()), novedades, Json)

    @AfterEach
    fun cerrar() = appwrite.close()

    @Test
    fun `ignora las novedades escritas por un guardado que no alcanzo a actualizar la solicitud`() {
        val solicitud = solicitudDePrueba(EstadoPrefactura.EN_REVISION)
        repositorio.guardar(solicitud)
        novedades.guardar("pendiente", FilaNovedad("PF-1", 0, "Falta la firma", "facturador", "2026-10-22T13:00:00Z"))
        assertEquals(solicitud, repositorio.buscar(solicitud.consecutivo))
        assertEquals(listOf(solicitud), repositorio.listar())
    }
}
