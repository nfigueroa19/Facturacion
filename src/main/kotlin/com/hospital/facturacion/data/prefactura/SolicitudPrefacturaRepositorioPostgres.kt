package com.hospital.facturacion.data.prefactura

import com.hospital.facturacion.data.postgres.consultar
import com.hospital.facturacion.data.postgres.ejecutar
import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.prefactura.ConsecutivoRadicado
import com.hospital.facturacion.domain.prefactura.SolicitudPrefactura
import com.hospital.facturacion.domain.prefactura.SolicitudPrefacturaRepositorio
import kotlinx.serialization.json.Json
import javax.sql.DataSource

/** Una fila por solicitud, con sus novedades en JSONB: cada guardado es una sola sentencia. */
class SolicitudPrefacturaRepositorioPostgres(private val fuente: DataSource, private val json: Json) : SolicitudPrefacturaRepositorio {
    override fun guardar(solicitud: SolicitudPrefactura) {
        fuente.ejecutar(GUARDAR_SOLICITUD, *solicitud.columnas(json))
    }

    override fun buscar(consecutivo: ConsecutivoRadicado) =
        fuente.consultar("SELECT * FROM solicitud_prefactura WHERE consecutivo = ?", consecutivo.valor) { it.solicitud(json) }
            .singleOrNull()

    override fun buscarPorCita(cita: CitaId) =
        fuente.consultar("SELECT * FROM solicitud_prefactura WHERE cita = ?", cita.valor) { it.solicitud(json) }.singleOrNull()

    override fun listar() = fuente.consultar("SELECT * FROM solicitud_prefactura ORDER BY fecha_envio") { it.solicitud(json) }
}
