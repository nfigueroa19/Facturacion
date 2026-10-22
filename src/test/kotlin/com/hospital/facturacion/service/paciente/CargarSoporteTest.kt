package com.hospital.facturacion.service.paciente

import com.hospital.facturacion.SoportesEnMemoria
import com.hospital.facturacion.citaIdDePrueba
import com.hospital.facturacion.domain.prefactura.EstadoPrefactura
import com.hospital.facturacion.domain.soporte.MotivoRechazoPdf
import com.hospital.facturacion.domain.soporte.TipoSoporte
import com.hospital.facturacion.repositorioCon
import com.hospital.facturacion.solicitudDePrueba
import kotlin.test.Test
import kotlin.test.assertEquals

class CargarSoporteTest {
    private val soportes = SoportesEnMemoria()
    private val pdf = "%PDF-1.4 orden".toByteArray()

    @Test
    fun `guarda un PDF valido antes de radicar y con novedades`() {
        assertEquals(ResultadoCarga.Cargado, CargarSoporte(repositorioCon(), soportes, 64).ejecutar(citaIdDePrueba, TipoSoporte.ORDEN_MEDICA, pdf))
        val conNovedades = repositorioCon(solicitudDePrueba(EstadoPrefactura.PENDIENTE))
        assertEquals(ResultadoCarga.Cargado, CargarSoporte(conNovedades, soportes, 64).ejecutar(citaIdDePrueba, TipoSoporte.AUTORIZACION, pdf))
        assertEquals(TipoSoporte.entries.toSet(), soportes.cargados(citaIdDePrueba))
    }

    @Test
    fun `rechaza archivos que no son PDF, demasiado grandes o con la solicitud ya aprobada`() {
        val cargar = CargarSoporte(repositorioCon(), soportes, 8)
        assertEquals(ResultadoCarga.Rechazado(MotivoRechazoPdf.NO_ES_PDF), cargar.ejecutar(citaIdDePrueba, TipoSoporte.AUTORIZACION, "foto".toByteArray()))
        assertEquals(ResultadoCarga.Rechazado(MotivoRechazoPdf.EXCEDE_TAMANO), cargar.ejecutar(citaIdDePrueba, TipoSoporte.AUTORIZACION, pdf))
        val aprobada = repositorioCon(solicitudDePrueba(EstadoPrefactura.APROBADO))
        assertEquals(ResultadoCarga.NoEditable, CargarSoporte(aprobada, soportes, 64).ejecutar(citaIdDePrueba, TipoSoporte.AUTORIZACION, pdf))
        assertEquals(emptySet(), soportes.cargados(citaIdDePrueba))
    }
}
