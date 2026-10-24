package com.hospital.facturacion.data.prefactura

import com.hospital.facturacion.data.appwrite.ConsultaAppwrite.Companion.ascendente
import com.hospital.facturacion.data.appwrite.ConsultaAppwrite.Companion.igual
import com.hospital.facturacion.data.appwrite.TablaAppwrite
import com.hospital.facturacion.data.appwrite.idAppwrite
import com.hospital.facturacion.data.cita.BorradoPorCita
import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.prefactura.ConsecutivoRadicado
import com.hospital.facturacion.domain.prefactura.SolicitudPrefactura
import com.hospital.facturacion.domain.prefactura.SolicitudPrefacturaRepositorio
import kotlinx.serialization.json.Json

/**
 * Solicitudes en la tabla `solicitudes` y una fila por novedad en `novedades`. Appwrite no une dos escrituras en una
 * sola operación: se escriben primero las novedades y al final la solicitud, que dice cuántas novedades valen.
 */
class SolicitudPrefacturaRepositorioAppwrite(
    private val solicitudes: TablaAppwrite<FilaSolicitud>,
    private val novedades: TablaAppwrite<FilaNovedad>,
    private val json: Json,
) : SolicitudPrefacturaRepositorio, BorradoPorCita {
    override fun guardar(solicitud: SolicitudPrefactura) {
        solicitud.filasNovedades().forEach { novedades.guardar(it.idFila(), it) }
        solicitudes.guardar(idAppwrite(solicitud.consecutivo.valor), solicitud.fila(json))
    }

    override fun buscar(consecutivo: ConsecutivoRadicado) = solicitudes.leer(idAppwrite(consecutivo.valor))?.conNovedades()

    override fun buscarPorCita(cita: CitaId) = solicitudes.buscar(igual("cita", cita.valor)).singleOrNull()?.conNovedades()

    override fun listar(): List<SolicitudPrefactura> {
        val porSolicitud = novedades.buscar().groupBy { it.solicitud }
        return solicitudes.buscar(ascendente("fecha_envio")).map { it.solicitud(porSolicitud[it.consecutivo].orEmpty(), json) }
    }

    override fun eliminar(cita: CitaId) = solicitudes.buscar(igual("cita", cita.valor)).forEach { fila ->
        novedades.buscar(igual("solicitud", fila.consecutivo)).forEach { novedades.eliminar(it.idFila()) }
        solicitudes.eliminar(idAppwrite(fila.consecutivo))
    }

    private fun FilaSolicitud.conNovedades() = solicitud(novedades.buscar(igual("solicitud", consecutivo)), json)
}

private fun FilaNovedad.idFila() = idAppwrite("$solicitud/$orden")
