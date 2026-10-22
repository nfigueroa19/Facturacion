package com.hospital.facturacion.service.paciente

import com.hospital.facturacion.AgendaDePrueba
import com.hospital.facturacion.SoportesEnMemoria
import com.hospital.facturacion.citaConCopago
import com.hospital.facturacion.data.copago.PagosCopagoMemoria
import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.prefactura.EstadoPrefactura
import com.hospital.facturacion.domain.sesion.Sesiones
import com.hospital.facturacion.domain.sesion.TokenSesion
import com.hospital.facturacion.repositorioCon
import com.hospital.facturacion.service.prefactura.Expediente
import com.hospital.facturacion.solicitudDePrueba
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SesionPacienteTest {
    private val abiertas = mutableListOf<CitaId>()
    private val sesiones = object : Sesiones<CitaId> {
        override fun abrir(sujeto: CitaId) = TokenSesion("token").also { abiertas += sujeto }

        override fun sujeto(token: TokenSesion) = null
    }

    @Test
    fun `abre la sesion de la cita con identificacion e ID de acceso, sin espacios de mas`() {
        val iniciar = IniciarSesionPaciente(AgendaDePrueba(citaConCopago), sesiones)
        assertEquals(TokenSesion("token"), iniciar.ejecutar(" 1000000001 ", " CODIGO "))
        assertNull(iniciar.ejecutar("1000000001", "OTRO"))
        assertEquals(listOf(citaConCopago.id), abiertas)
    }

    @Test
    fun `la prefactura del paciente reune cita, soportes, pago y solicitud`() {
        val solicitud = solicitudDePrueba(EstadoPrefactura.PENDIENTE)
        val consultar = ConsultarPrefacturaPaciente(repositorioCon(solicitud), SoportesEnMemoria(), PagosCopagoMemoria())
        assertEquals(PrefacturaPaciente(Expediente(citaConCopago, emptySet(), null), solicitud), consultar.ejecutar(citaConCopago))
    }
}
