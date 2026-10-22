package com.hospital.facturacion.service.facturador

import com.hospital.facturacion.domain.prefactura.ConsecutivoRadicado
import com.hospital.facturacion.domain.prefactura.SolicitudPrefacturaRepositorio
import com.hospital.facturacion.domain.soporte.AlmacenSoportes
import com.hospital.facturacion.domain.soporte.DocumentoPdf
import com.hospital.facturacion.domain.soporte.TipoSoporte

class ObtenerSoporte(private val solicitudes: SolicitudPrefacturaRepositorio, private val almacen: AlmacenSoportes) {
    fun ejecutar(consecutivo: ConsecutivoRadicado, tipo: TipoSoporte): DocumentoPdf? =
        solicitudes.buscar(consecutivo)?.let { almacen.leer(it.cita, tipo) }
}
