package com.hospital.facturacion.service.digiturno

import com.hospital.facturacion.AgendaDePrueba
import com.hospital.facturacion.citaConCopago
import com.hospital.facturacion.data.digiturno.GeneradorTurnosMemoria
import com.hospital.facturacion.domain.cita.Cita
import com.hospital.facturacion.domain.digiturno.DestinoTurno
import com.hospital.facturacion.domain.digiturno.MotivoVentanilla
import com.hospital.facturacion.domain.digiturno.Turno
import com.hospital.facturacion.domain.prefactura.EstadoPrefactura
import com.hospital.facturacion.domain.prefactura.Novedad
import com.hospital.facturacion.relojDePrueba
import com.hospital.facturacion.repositorioCon
import com.hospital.facturacion.solicitudDePrueba
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AtenderLlegadaTest {
    private val citaHoy = citaConCopago.copy(fecha = LocalDate.of(2026, 10, 22))

    private fun atender(estado: EstadoPrefactura?, cita: Cita = citaHoy) = repositorioCon(*listOfNotNull(estado?.let(::solicitudDePrueba)).toTypedArray())
        .let { it to AtenderLlegada(AgendaDePrueba(cita), it, GeneradorTurnosMemoria(), relojDePrueba) }

    @Test
    fun `con validacion aprobada confirma la llegada y asigna turno de preconsulta`() {
        val (repositorio, digiturno) = atender(EstadoPrefactura.APROBADO)
        val preconsulta = assertIs<ResultadoDigiturno.Preconsulta>(digiturno.ejecutar(" 1000000001 "))
        assertEquals(Turno("P-001", DestinoTurno.PRECONSULTA), preconsulta.turno)
        assertEquals(EstadoPrefactura.LISTO_PARA_FACTURAR, repositorio.buscarPorCita(citaHoy.id)?.estado)
        assertEquals(Turno("P-002", DestinoTurno.PRECONSULTA), digiturno.ejecutar("1000000001").turno)
    }

    @Test
    fun `envia a ventanilla con el motivo y las novedades del facturador`() {
        val novedad = requireNotNull(Novedad.de("Falta la autorización", "facturador", relojDePrueba.instant()))
        val repositorio = repositorioCon(solicitudDePrueba(EstadoPrefactura.PENDIENTE).copy(novedades = listOf(novedad)))
        val ventanilla = AtenderLlegada(AgendaDePrueba(citaHoy), repositorio, GeneradorTurnosMemoria(), relojDePrueba).ejecutar("1000000001")
        assertEquals(ResultadoDigiturno.Ventanilla(Turno("F-001", DestinoTurno.FACTURACION), MotivoVentanilla.CON_NOVEDADES, citaHoy, listOf(novedad)), ventanilla)
        assertEquals(EstadoPrefactura.PENDIENTE, repositorio.buscarPorCita(citaHoy.id)?.estado)
    }

    @Test
    fun `sin cita hoy, sin radicar, en revision o con el ingreso anulado tambien va a ventanilla`() {
        fun motivo(estado: EstadoPrefactura?, cita: Cita = citaHoy) =
            assertIs<ResultadoDigiturno.Ventanilla>(atender(estado, cita).second.ejecutar("1000000001")).motivo
        assertEquals(MotivoVentanilla.SIN_CITA_HOY, motivo(EstadoPrefactura.APROBADO, citaConCopago))
        assertEquals(MotivoVentanilla.SIN_RADICAR, motivo(null))
        assertEquals(MotivoVentanilla.EN_REVISION, motivo(EstadoPrefactura.EN_REVISION))
        assertEquals(MotivoVentanilla.INGRESO_ANULADO, motivo(EstadoPrefactura.INGRESO_ANULADO))
    }
}
