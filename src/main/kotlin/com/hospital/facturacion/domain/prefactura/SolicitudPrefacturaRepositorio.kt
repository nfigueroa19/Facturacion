package com.hospital.facturacion.domain.prefactura

import com.hospital.facturacion.domain.cita.CitaId

interface SolicitudPrefacturaRepositorio {
    fun guardar(solicitud: SolicitudPrefactura)

    fun buscar(consecutivo: ConsecutivoRadicado): SolicitudPrefactura?

    fun buscarPorCita(cita: CitaId): SolicitudPrefactura?

    fun listar(): List<SolicitudPrefactura>
}
