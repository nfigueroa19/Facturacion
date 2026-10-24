package com.hospital.facturacion.service.facturador

import com.hospital.facturacion.domain.cita.AgendaCitas
import com.hospital.facturacion.domain.facturador.Facturador
import com.hospital.facturacion.domain.prefactura.ConsecutivoRadicado
import com.hospital.facturacion.domain.prefactura.NumeroDinamica
import com.hospital.facturacion.domain.prefactura.Registro
import com.hospital.facturacion.domain.prefactura.SolicitudPrefacturaRepositorio
import com.hospital.facturacion.domain.prefactura.registrarFactura
import java.time.Clock

/** Registra el número de factura de Dinámica y cierra la solicitud en FACTURADO. */
class RegistrarFactura(
    private val solicitudes: SolicitudPrefacturaRepositorio,
    private val agenda: AgendaCitas,
    private val reloj: Clock,
) {
    fun ejecutar(consecutivo: ConsecutivoRadicado, numero: String, facturador: Facturador): ResultadoAccion {
        val factura = NumeroDinamica.de(numero) ?: return ResultadoAccion.DatoInvalido
        return solicitudes.aplicarElDiaDeLaCita(consecutivo, agenda, reloj) {
            it.registrarFactura(Registro(factura, facturador.usuario, reloj.instant()))
        }
    }
}
