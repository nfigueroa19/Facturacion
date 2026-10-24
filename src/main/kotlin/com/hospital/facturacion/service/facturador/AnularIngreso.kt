package com.hospital.facturacion.service.facturador

import com.hospital.facturacion.domain.facturador.Facturador
import com.hospital.facturacion.domain.prefactura.ConsecutivoRadicado
import com.hospital.facturacion.domain.prefactura.Observacion
import com.hospital.facturacion.domain.prefactura.Registro
import com.hospital.facturacion.domain.prefactura.SolicitudPrefacturaRepositorio
import com.hospital.facturacion.domain.prefactura.anularIngreso
import java.time.Clock

/**
 * Anula el ingreso por inasistencia (RN-13). No se limita la hora porque las reglas institucionales de anulación
 * no están definidas (P-03); el motivo queda registrado.
 */
class AnularIngreso(private val solicitudes: SolicitudPrefacturaRepositorio, private val reloj: Clock) {
    fun ejecutar(consecutivo: ConsecutivoRadicado, motivo: String, facturador: Facturador): ResultadoAccion {
        val observacion = Observacion.de(motivo) ?: return ResultadoAccion.DatoInvalido
        val solicitud = solicitudes.buscar(consecutivo) ?: return ResultadoAccion.NoEncontrada
        return solicitudes.aplicar(solicitud.anularIngreso(Registro(observacion, facturador.usuario, reloj.instant())))
    }
}
