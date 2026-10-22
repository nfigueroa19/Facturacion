package com.hospital.facturacion.service.facturador

import com.hospital.facturacion.domain.facturador.Facturador
import com.hospital.facturacion.domain.prefactura.ConsecutivoRadicado
import com.hospital.facturacion.domain.prefactura.Novedad
import com.hospital.facturacion.domain.prefactura.SolicitudPrefacturaRepositorio
import java.time.Clock

/**
 * La solicitud queda PENDIENTE y el paciente ve la novedad al ingresar. El aviso al digiturno y el canal
 * de notificación al paciente (RN-12) siguen sin definir (P-11).
 */
class RegistrarNovedad(private val solicitudes: SolicitudPrefacturaRepositorio, private val reloj: Clock) {
    fun ejecutar(consecutivo: ConsecutivoRadicado, descripcion: String, facturador: Facturador): ResultadoAccion {
        val solicitud = solicitudes.buscar(consecutivo) ?: return ResultadoAccion.NoEncontrada
        val novedad = Novedad.de(descripcion, facturador.usuario, reloj.instant()) ?: return ResultadoAccion.NovedadInvalida
        return solicitudes.aplicar(solicitud.registrarNovedad(novedad))
    }
}
