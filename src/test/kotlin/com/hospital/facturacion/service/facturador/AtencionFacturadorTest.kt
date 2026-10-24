package com.hospital.facturacion.service.facturador

import com.hospital.facturacion.AgendaDePrueba
import com.hospital.facturacion.citaConCopago
import com.hospital.facturacion.domain.facturador.Facturador
import com.hospital.facturacion.domain.prefactura.ConsecutivoRadicado
import com.hospital.facturacion.domain.prefactura.EstadoPrefactura
import com.hospital.facturacion.domain.prefactura.MotivoRechazoAtencion
import com.hospital.facturacion.relojDePrueba
import com.hospital.facturacion.repositorioCon
import com.hospital.facturacion.solicitudDePrueba
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/** Ingreso, intentos de facturación, factura y anulación: el reloj de prueba marca 2026-10-22. */
class AtencionFacturadorTest {
    private val facturador = Facturador("facturador", "Facturador de prueba")
    private val radicado = ConsecutivoRadicado("PF-1")
    private val citaHoy = citaConCopago.copy(fecha = LocalDate.of(2026, 10, 22))
    private val repositorio = repositorioCon(solicitudDePrueba(EstadoPrefactura.APROBADO))
    private val agendaHoy = AgendaDePrueba(citaHoy)

    @Test
    fun `registra el ingreso, confirma la llegada, reintenta tras un error y factura el dia de la cita`() {
        assertEquals(ResultadoAccion.DatoInvalido, RegistrarIngreso(repositorio, agendaHoy, relojDePrueba).ejecutar(radicado, " ", facturador))
        assertIs<ResultadoAccion.Aplicada>(RegistrarIngreso(repositorio, agendaHoy, relojDePrueba).ejecutar(radicado, "ING-77", facturador))
        ConfirmarLlegada(repositorio).ejecutar(radicado)
        val intento = RegistrarIntentoFacturacion(repositorio, agendaHoy, relojDePrueba)
        assertIs<ResultadoAccion.Aplicada>(intento.ejecutar(radicado, "Dinámica no encontró el contrato", facturador))
        assertIs<ResultadoAccion.Aplicada>(RegistrarFactura(repositorio, agendaHoy, relojDePrueba).ejecutar(radicado, "FE-1001", facturador))
        val facturada = requireNotNull(repositorio.buscar(radicado))
        assertEquals(EstadoPrefactura.FACTURADO, facturada.estado)
        assertEquals("ING-77", facturada.atencion.ingreso?.valor?.valor)
        assertEquals(listOf("Dinámica no encontró el contrato"), facturada.atencion.intentosFallidos.map { it.valor.texto })
        assertEquals("FE-1001" to relojDePrueba.instant(), facturada.atencion.factura?.let { it.valor.valor to it.fecha })
    }

    @Test
    fun `no abre ingreso ni factura antes del dia de la cita`() {
        val manana = AgendaDePrueba(citaConCopago)
        val fuera = ResultadoAccion.ReglaIncumplida(MotivoRechazoAtencion.FUERA_DEL_DIA_DE_LA_CITA)
        assertEquals(fuera, RegistrarIngreso(repositorio, manana, relojDePrueba).ejecutar(radicado, "ING-1", facturador))
        assertEquals(fuera, RegistrarFactura(repositorio, manana, relojDePrueba).ejecutar(radicado, "FE-1", facturador))
        assertEquals(fuera, RegistrarIntentoFacturacion(repositorio, manana, relojDePrueba).ejecutar(radicado, "Error", facturador))
        assertEquals(ResultadoAccion.NoEncontrada, RegistrarIngreso(repositorio, manana, relojDePrueba).ejecutar(ConsecutivoRadicado("PF-9"), "ING-1", facturador))
    }
}
