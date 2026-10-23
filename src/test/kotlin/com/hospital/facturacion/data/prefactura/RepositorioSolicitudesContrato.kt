package com.hospital.facturacion.data.prefactura

import com.hospital.facturacion.citaIdDePrueba
import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.prefactura.CambioEstado
import com.hospital.facturacion.domain.prefactura.ConsecutivoRadicado
import com.hospital.facturacion.domain.prefactura.EstadoPrefactura
import com.hospital.facturacion.domain.prefactura.Novedad
import com.hospital.facturacion.domain.prefactura.SolicitudPrefactura
import com.hospital.facturacion.domain.prefactura.SolicitudPrefacturaRepositorio
import com.hospital.facturacion.domain.prefactura.Validacion
import com.hospital.facturacion.solicitudDePrueba
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

/** Lo que cualquier adaptador persistente del repositorio de solicitudes debe cumplir. */
abstract class RepositorioSolicitudesContrato {
    abstract val repositorio: SolicitudPrefacturaRepositorio
    private val fecha = Instant.parse("2026-10-22T13:00:00Z")

    private fun CambioEstado.aplicado(): SolicitudPrefactura = assertIs<CambioEstado.Aplicado>(this).solicitud

    @Test
    fun `guarda y recupera por consecutivo de radicado y por cita`() {
        val solicitud = solicitudDePrueba(EstadoPrefactura.EN_REVISION)
        repositorio.guardar(solicitud)
        assertEquals(solicitud, repositorio.buscar(ConsecutivoRadicado("PF-1")))
        assertEquals(solicitud, repositorio.buscarPorCita(citaIdDePrueba))
        assertEquals(listOf(solicitud), repositorio.listar())
        assertNull(repositorio.buscar(ConsecutivoRadicado("PF-2")))
        assertNull(repositorio.buscarPorCita(requireNotNull(CitaId.de("otra"))))
    }

    @Test
    fun `conserva las novedades en orden, la correccion y la validacion del facturador`() {
        val primera = requireNotNull(Novedad.de("Falta la firma", "facturador", fecha))
        val segunda = requireNotNull(Novedad.de("Orden ilegible", "facturador", fecha.plusSeconds(60)))
        val corregida = solicitudDePrueba(EstadoPrefactura.EN_REVISION)
            .registrarNovedad(primera).aplicado()
            .corregir(solicitudDePrueba(EstadoPrefactura.EN_REVISION).datos, fecha).aplicado()
            .registrarNovedad(segunda).aplicado()
            .corregir(solicitudDePrueba(EstadoPrefactura.EN_REVISION).datos, fecha.plusSeconds(120)).aplicado()
        val aprobada = corregida.aprobar(Validacion("facturador", fecha.plusSeconds(180))).aplicado()
        repositorio.guardar(corregida)
        repositorio.guardar(aprobada)
        assertEquals(aprobada, repositorio.buscar(aprobada.consecutivo))
        assertEquals(listOf(primera, segunda), repositorio.buscarPorCita(citaIdDePrueba)?.novedades)
    }
}
