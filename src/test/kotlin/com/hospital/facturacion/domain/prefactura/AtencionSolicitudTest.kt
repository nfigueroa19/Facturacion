package com.hospital.facturacion.domain.prefactura

import com.hospital.facturacion.solicitudDePrueba
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class AtencionSolicitudTest {
    private val fecha = Instant.parse("2026-10-22T13:00:00Z")
    private val ingreso = Registro(requireNotNull(NumeroDinamica.de("ING-1")), "facturador", fecha)
    private val factura = Registro(requireNotNull(NumeroDinamica.de("FE-99")), "facturador", fecha)
    private val error = Registro(requireNotNull(Observacion.de("Dinámica rechazó el NIT de la EPS")), "facturador", fecha)

    private fun CambioEstado.aplicado(): SolicitudPrefactura = assertIs<CambioEstado.Aplicado>(this).solicitud

    @Test
    fun `el ingreso se registra una vez, antes o despues de la llegada, y solo con la solicitud aprobada`() {
        val conIngreso = solicitudDePrueba(EstadoPrefactura.APROBADO).registrarIngreso(ingreso).aplicado()
        assertEquals(ingreso, conIngreso.atencion.ingreso)
        assertEquals(CambioEstado.Incumple(MotivoRechazoAtencion.INGRESO_YA_REGISTRADO), conIngreso.registrarIngreso(ingreso))
        assertEquals(ingreso, solicitudDePrueba(EstadoPrefactura.LISTO_PARA_FACTURAR).registrarIngreso(ingreso).aplicado().atencion.ingreso)
        assertEquals(CambioEstado.Incumple(MotivoRechazoAtencion.ESTADO_NO_PERMITE), solicitudDePrueba(EstadoPrefactura.EN_REVISION).registrarIngreso(ingreso))
    }

    @Test
    fun `factura despues de un intento fallido y exige ingreso y llegada confirmada`() {
        val lista = solicitudDePrueba(EstadoPrefactura.LISTO_PARA_FACTURAR)
        assertEquals(CambioEstado.Incumple(MotivoRechazoAtencion.SIN_INGRESO), lista.registrarFactura(factura))
        assertEquals(CambioEstado.Incumple(MotivoRechazoAtencion.SIN_INGRESO), lista.registrarIntentoFallido(error))
        val conError = lista.registrarIngreso(ingreso).aplicado().registrarIntentoFallido(error).aplicado()
        assertEquals(EstadoPrefactura.LISTO_PARA_FACTURAR, conError.estado)
        val facturada = conError.registrarFactura(factura).aplicado()
        assertEquals(EstadoPrefactura.FACTURADO, facturada.estado)
        assertEquals(Atencion(ingreso, listOf(error), factura), facturada.atencion)
        val aprobada = solicitudDePrueba(EstadoPrefactura.APROBADO).registrarIngreso(ingreso).aplicado()
        assertEquals(CambioEstado.Rechazado(EstadoPrefactura.APROBADO, EstadoPrefactura.FACTURADO), aprobada.registrarFactura(factura))
        assertEquals(CambioEstado.Incumple(MotivoRechazoAtencion.ESTADO_NO_PERMITE), aprobada.registrarIntentoFallido(error))
    }
}
