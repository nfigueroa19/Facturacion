package com.hospital.facturacion.domain.prefactura

interface SolicitudPrefacturaRepositorio {
    fun guardar(solicitud: SolicitudPrefactura)

    fun buscar(consecutivo: ConsecutivoRadicado): SolicitudPrefactura?
}
