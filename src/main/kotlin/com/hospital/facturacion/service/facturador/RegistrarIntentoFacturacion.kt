package com.hospital.facturacion.service.facturador

import com.hospital.facturacion.domain.cita.AgendaCitas
import com.hospital.facturacion.domain.facturador.Facturador
import com.hospital.facturacion.domain.prefactura.ConsecutivoRadicado
import com.hospital.facturacion.domain.prefactura.Observacion
import com.hospital.facturacion.domain.prefactura.Registro
import com.hospital.facturacion.domain.prefactura.SolicitudPrefacturaRepositorio
import com.hospital.facturacion.domain.prefactura.registrarIntentoFallido
import java.time.Clock

/** Deja constancia del error que devolvió Dinámica; el facturador lo resuelve y vuelve a cargar. */
class RegistrarIntentoFacturacion(
    private val solicitudes: SolicitudPrefacturaRepositorio,
    private val agenda: AgendaCitas,
    private val reloj: Clock,
) {
    fun ejecutar(consecutivo: ConsecutivoRadicado, error: String, facturador: Facturador): ResultadoAccion {
        val observacion = Observacion.de(error) ?: return ResultadoAccion.DatoInvalido
        return solicitudes.aplicarElDiaDeLaCita(consecutivo, agenda, reloj) {
            it.registrarIntentoFallido(Registro(observacion, facturador.usuario, reloj.instant()))
        }
    }
}
