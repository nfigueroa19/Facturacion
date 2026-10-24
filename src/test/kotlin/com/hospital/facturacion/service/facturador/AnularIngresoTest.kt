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

class AnularIngresoTest {
    private val facturador = Facturador("facturador", "Facturador de prueba")
    private val radicado = ConsecutivoRadicado("PF-1")
    private val repositorio = repositorioCon(solicitudDePrueba(EstadoPrefactura.APROBADO))
    private val agendaHoy = AgendaDePrueba(citaConCopago.copy(fecha = LocalDate.of(2026, 10, 22)))

    @Test
    fun `anula el ingreso con motivo cuando el paciente no llega`() {
        val anular = AnularIngreso(repositorio, relojDePrueba)
        assertEquals(ResultadoAccion.ReglaIncumplida(MotivoRechazoAtencion.SIN_INGRESO), anular.ejecutar(radicado, "No llegó", facturador))
        RegistrarIngreso(repositorio, agendaHoy, relojDePrueba).ejecutar(radicado, "ING-5", facturador)
        assertEquals(ResultadoAccion.DatoInvalido, anular.ejecutar(radicado, "", facturador))
        assertIs<ResultadoAccion.Aplicada>(anular.ejecutar(radicado, "No llegó a la cita", facturador))
        assertEquals(EstadoPrefactura.INGRESO_ANULADO, repositorio.buscar(radicado)?.estado)
        assertEquals("No llegó a la cita", repositorio.buscar(radicado)?.atencion?.anulacion?.valor?.texto)
    }
}
