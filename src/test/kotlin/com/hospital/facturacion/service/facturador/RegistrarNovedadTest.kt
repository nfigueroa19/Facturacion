package com.hospital.facturacion.service.facturador

import com.hospital.facturacion.domain.facturador.Facturador
import com.hospital.facturacion.domain.prefactura.ConsecutivoRadicado
import com.hospital.facturacion.domain.prefactura.EstadoPrefactura
import com.hospital.facturacion.relojDePrueba
import com.hospital.facturacion.repositorioCon
import com.hospital.facturacion.solicitudDePrueba
import kotlin.test.Test
import kotlin.test.assertEquals

class RegistrarNovedadTest {
    private val facturador = Facturador("facturador", "Facturador de prueba")
    private val radicado = ConsecutivoRadicado("PF-1")

    @Test
    fun `deja la solicitud PENDIENTE con la novedad, el facturador y la fecha`() {
        val repositorio = repositorioCon(solicitudDePrueba(EstadoPrefactura.EN_REVISION))
        RegistrarNovedad(repositorio, relojDePrueba).ejecutar(radicado, "La autorización está vencida", facturador)
        val pendiente = requireNotNull(repositorio.buscar(radicado))
        assertEquals(EstadoPrefactura.PENDIENTE, pendiente.estado)
        assertEquals(listOf("La autorización está vencida" to "facturador"), pendiente.novedades.map { it.descripcion to it.facturador })
        assertEquals(relojDePrueba.instant(), pendiente.novedades.single().fecha)
    }

    @Test
    fun `rechaza novedades vacias y solicitudes que no estan en revision`() {
        val aprobada = repositorioCon(solicitudDePrueba(EstadoPrefactura.APROBADO))
        val registrar = RegistrarNovedad(aprobada, relojDePrueba)
        assertEquals(ResultadoAccion.NovedadInvalida, registrar.ejecutar(radicado, "  ", facturador))
        assertEquals(ResultadoAccion.TransicionInvalida(EstadoPrefactura.APROBADO, EstadoPrefactura.PENDIENTE), registrar.ejecutar(radicado, "Dato errado", facturador))
    }
}
