package com.hospital.facturacion.data.prefactura

import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.prefactura.ConsecutivoRadicado
import com.hospital.facturacion.domain.prefactura.SolicitudPrefactura
import com.hospital.facturacion.domain.prefactura.SolicitudPrefacturaRepositorio
import java.util.concurrent.ConcurrentHashMap

/** Doble en memoria para las pruebas de los casos de uso. La aplicación usa `SolicitudPrefacturaRepositorioPostgres`. */
class SolicitudPrefacturaRepositorioMemoria : SolicitudPrefacturaRepositorio {
    private val solicitudes = ConcurrentHashMap<ConsecutivoRadicado, SolicitudPrefactura>()

    override fun guardar(solicitud: SolicitudPrefactura) {
        solicitudes[solicitud.consecutivo] = solicitud
    }

    override fun buscar(consecutivo: ConsecutivoRadicado): SolicitudPrefactura? = solicitudes[consecutivo]

    override fun buscarPorCita(cita: CitaId): SolicitudPrefactura? = solicitudes.values.firstOrNull { it.cita == cita }

    override fun listar(): List<SolicitudPrefactura> = solicitudes.values.toList()
}
