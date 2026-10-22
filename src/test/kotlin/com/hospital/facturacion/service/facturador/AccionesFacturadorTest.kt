package com.hospital.facturacion.service.facturador

import com.hospital.facturacion.AgendaDePrueba
import com.hospital.facturacion.citaConCopago
import com.hospital.facturacion.domain.facturador.Facturador
import com.hospital.facturacion.domain.prefactura.ConsecutivoRadicado
import com.hospital.facturacion.domain.prefactura.EstadoPrefactura
import com.hospital.facturacion.domain.prefactura.ItemRevision
import com.hospital.facturacion.domain.prefactura.Validacion
import com.hospital.facturacion.relojDePrueba
import com.hospital.facturacion.repositorioCon
import com.hospital.facturacion.solicitudDePrueba
import kotlin.test.Test
import kotlin.test.assertEquals

class AccionesFacturadorTest {
    private val facturador = Facturador("facturador", "Facturador de prueba")
    private val radicado = ConsecutivoRadicado("PF-1")
    private val repositorio = repositorioCon(solicitudDePrueba(EstadoPrefactura.EN_REVISION))
    private val aprobar = AprobarSolicitud(repositorio, AgendaDePrueba(citaConCopago), relojDePrueba)

    @Test
    fun `aprueba solo con el checklist completo y registra quien y cuando`() {
        val sinCopago = ItemRevision.entries.toSet() - ItemRevision.COPAGO
        assertEquals(ResultadoAccion.ChecklistIncompleto(setOf(ItemRevision.COPAGO)), aprobar.ejecutar(radicado, sinCopago, facturador))
        val aprobada = solicitudDePrueba(EstadoPrefactura.APROBADO).copy(validacion = Validacion("facturador", relojDePrueba.instant()))
        assertEquals(ResultadoAccion.Aplicada(aprobada), aprobar.ejecutar(radicado, ItemRevision.entries.toSet(), facturador))
        assertEquals(aprobada, repositorio.buscar(radicado))
    }

    @Test
    fun `la llegada confirmada pasa una aprobada a LISTO_PARA_FACTURAR y no salta la aprobacion`() {
        val confirmar = ConfirmarLlegada(repositorio)
        assertEquals(ResultadoAccion.TransicionInvalida(EstadoPrefactura.EN_REVISION, EstadoPrefactura.LISTO_PARA_FACTURAR), confirmar.ejecutar(radicado))
        aprobar.ejecutar(radicado, ItemRevision.entries.toSet(), facturador)
        confirmar.ejecutar(radicado)
        assertEquals(EstadoPrefactura.LISTO_PARA_FACTURAR, repositorio.buscar(radicado)?.estado)
        assertEquals(ResultadoAccion.NoEncontrada, confirmar.ejecutar(ConsecutivoRadicado("PF-404")))
    }
}
