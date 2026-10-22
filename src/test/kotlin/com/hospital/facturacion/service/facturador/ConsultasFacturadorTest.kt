package com.hospital.facturacion.service.facturador

import com.hospital.facturacion.AgendaDePrueba
import com.hospital.facturacion.SoportesEnMemoria
import com.hospital.facturacion.citaConCopago
import com.hospital.facturacion.data.copago.PagosCopagoMemoria
import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.facturador.DirectorioFacturadores
import com.hospital.facturacion.domain.facturador.Facturador
import com.hospital.facturacion.domain.prefactura.ConsecutivoRadicado
import com.hospital.facturacion.domain.prefactura.EstadoPrefactura
import com.hospital.facturacion.domain.sesion.Sesiones
import com.hospital.facturacion.domain.sesion.TokenSesion
import com.hospital.facturacion.domain.soporte.TipoSoporte
import com.hospital.facturacion.repositorioCon
import com.hospital.facturacion.service.prefactura.Expediente
import com.hospital.facturacion.solicitudDePrueba
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ConsultasFacturadorTest {
    private val enRevision = solicitudDePrueba(EstadoPrefactura.EN_REVISION).copy(fechaEnvio = Instant.MAX)
    private val antigua = solicitudDePrueba(EstadoPrefactura.EN_REVISION).copy(consecutivo = ConsecutivoRadicado("PF-0"))
    private val sinCita = antigua.copy(consecutivo = ConsecutivoRadicado("PF-X"), cita = requireNotNull(CitaId.de("fuera-de-agenda")))
    private val aprobada = enRevision.copy(consecutivo = ConsecutivoRadicado("PF-2"), estado = EstadoPrefactura.APROBADO)
    private val repositorio = repositorioCon(enRevision, antigua, sinCita, aprobada)
    private val soportes = SoportesEnMemoria(TipoSoporte.AUTORIZACION)
    private val expediente = Expediente(citaConCopago, setOf(TipoSoporte.AUTORIZACION), null)

    @Test
    fun `lista por estado de la mas antigua a la mas reciente con su expediente`() {
        val listar = ListarSolicitudes(repositorio, AgendaDePrueba(citaConCopago), soportes, PagosCopagoMemoria())
        assertEquals(listOf(antigua, enRevision).map { SolicitudRevisable(it, expediente) }, listar.ejecutar(EstadoPrefactura.EN_REVISION))
        assertEquals(3, listar.ejecutar(null).size)
    }

    @Test
    fun `consulta el detalle y el PDF de una solicitud`() {
        val consultar = ConsultarSolicitud(repositorio, AgendaDePrueba(citaConCopago), soportes, PagosCopagoMemoria())
        assertEquals(SolicitudRevisable(aprobada, expediente), consultar.ejecutar(ConsecutivoRadicado("PF-2")))
        val obtener = ObtenerSoporte(repositorio, soportes)
        assertEquals(soportes.archivos[TipoSoporte.AUTORIZACION], obtener.ejecutar(ConsecutivoRadicado("PF-2"), TipoSoporte.AUTORIZACION))
        assertNull(obtener.ejecutar(ConsecutivoRadicado("PF-2"), TipoSoporte.ORDEN_MEDICA))
    }
}

class IniciarSesionFacturadorTest {
    private val facturador = Facturador("facturador", "Facturador de prueba")
    private val sesiones = object : Sesiones<Facturador> {
        override fun abrir(sujeto: Facturador) = TokenSesion("t")

        override fun sujeto(token: TokenSesion) = null
    }

    @Test
    fun `abre sesion solo con usuario y clave validos`() {
        val directorio = DirectorioFacturadores { usuario, clave -> facturador.takeIf { usuario == "facturador" && clave == "clave" } }
        val iniciar = IniciarSesionFacturador(directorio, sesiones)
        assertEquals(SesionFacturador(TokenSesion("t"), facturador), iniciar.ejecutar(" facturador ", "clave"))
        assertNull(iniciar.ejecutar("facturador", "otra"))
    }
}
