package com.hospital.facturacion.service.facturador

import com.hospital.facturacion.domain.cita.AgendaCitas
import com.hospital.facturacion.domain.facturador.Facturador
import com.hospital.facturacion.domain.prefactura.ConsecutivoRadicado
import com.hospital.facturacion.domain.prefactura.ItemRevision
import com.hospital.facturacion.domain.prefactura.SolicitudPrefacturaRepositorio
import com.hospital.facturacion.domain.prefactura.Validacion
import java.time.Clock

/** Aprobar no crea el ingreso ni factura en Dinámica (RN-02). */
class AprobarSolicitud(
    private val solicitudes: SolicitudPrefacturaRepositorio,
    private val agenda: AgendaCitas,
    private val reloj: Clock,
) {
    fun ejecutar(consecutivo: ConsecutivoRadicado, revisados: Set<ItemRevision>, facturador: Facturador): ResultadoAccion {
        val solicitud = solicitudes.buscar(consecutivo) ?: return ResultadoAccion.NoEncontrada
        val cita = agenda.buscar(solicitud.cita) ?: return ResultadoAccion.NoEncontrada
        val faltantes = ItemRevision.requeridosPara(cita) - revisados
        if (faltantes.isNotEmpty()) return ResultadoAccion.ChecklistIncompleto(faltantes)
        return solicitudes.aplicar(solicitud.aprobar(Validacion(facturador.usuario, reloj.instant())))
    }
}
