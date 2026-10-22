package com.hospital.facturacion.service.paciente

import com.hospital.facturacion.SoportesEnMemoria
import com.hospital.facturacion.citaIdDePrueba
import com.hospital.facturacion.data.prefactura.SolicitudPrefacturaRepositorioMemoria
import com.hospital.facturacion.datosDePrueba
import com.hospital.facturacion.domain.formulario.ErrorCampo
import com.hospital.facturacion.domain.prefactura.ConsecutivoRadicado
import com.hospital.facturacion.domain.prefactura.EstadoPrefactura
import com.hospital.facturacion.domain.prefactura.SolicitudPrefactura
import com.hospital.facturacion.domain.soporte.TipoSoporte
import com.hospital.facturacion.formularioDePrueba
import com.hospital.facturacion.relojDePrueba
import com.hospital.facturacion.repositorioCon
import com.hospital.facturacion.solicitudDePrueba
import kotlin.test.Test
import kotlin.test.assertEquals

class RadicarSolicitudTest {
    private val valores = mapOf("nombre-padre" to "Padre de prueba")
    private val completos = SoportesEnMemoria(*TipoSoporte.entries.toTypedArray())

    private fun radicar(repositorio: SolicitudPrefacturaRepositorioMemoria, soportes: SoportesEnMemoria = completos, datos: Map<String, String> = valores) =
        RadicarSolicitud(formularioDePrueba, repositorio, soportes, { ConsecutivoRadicado("PF-9") }, relojDePrueba).ejecutar(citaIdDePrueba, datos)

    @Test
    fun `radica en EN_REVISION con consecutivo y fecha de envio`() {
        val repositorio = repositorioCon()
        val esperada = SolicitudPrefactura(ConsecutivoRadicado("PF-9"), citaIdDePrueba, EstadoPrefactura.EN_REVISION, relojDePrueba.instant(), datosDePrueba)
        assertEquals(ResultadoRadicacion.Radicada(esperada), radicar(repositorio))
        assertEquals(esperada, repositorio.buscarPorCita(citaIdDePrueba))
    }

    @Test
    fun `un reenvio tras novedades conserva el consecutivo y vuelve a EN_REVISION`() {
        val resultado = radicar(repositorioCon(solicitudDePrueba(EstadoPrefactura.PENDIENTE)))
        val radicada = (resultado as ResultadoRadicacion.Radicada).solicitud
        assertEquals(ConsecutivoRadicado("PF-1") to EstadoPrefactura.EN_REVISION, radicada.consecutivo to radicada.estado)
    }

    @Test
    fun `no radica con datos invalidos, sin los dos soportes ni sobre una solicitud ya en revision`() {
        assertEquals(ResultadoRadicacion.DatosInvalidos(mapOf("nombre-padre" to ErrorCampo.FALTA)), radicar(repositorioCon(), datos = emptyMap()))
        assertEquals(ResultadoRadicacion.FaltanSoportes(setOf(TipoSoporte.ORDEN_MEDICA)), radicar(repositorioCon(), SoportesEnMemoria(TipoSoporte.AUTORIZACION)))
        assertEquals(ResultadoRadicacion.NoEditable, radicar(repositorioCon(solicitudDePrueba(EstadoPrefactura.EN_REVISION))))
    }
}
