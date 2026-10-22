package com.hospital.facturacion.data.prefactura

import com.hospital.facturacion.domain.prefactura.ConsecutivoRadicado
import com.hospital.facturacion.domain.prefactura.SolicitudPrefactura
import com.hospital.facturacion.domain.prefactura.SolicitudPrefacturaRepositorio
import java.util.concurrent.ConcurrentHashMap

/** Persistencia del piloto con datos ficticios. Se reemplaza por el adaptador PostgreSQL. */
class SolicitudPrefacturaRepositorioMemoria : SolicitudPrefacturaRepositorio {
    private val solicitudes = ConcurrentHashMap<ConsecutivoRadicado, SolicitudPrefactura>()

    override fun guardar(solicitud: SolicitudPrefactura) {
        solicitudes[solicitud.consecutivo] = solicitud
    }

    override fun buscar(consecutivo: ConsecutivoRadicado): SolicitudPrefactura? = solicitudes[consecutivo]
}
