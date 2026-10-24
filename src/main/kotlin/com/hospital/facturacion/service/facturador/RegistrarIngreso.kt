package com.hospital.facturacion.service.facturador

import com.hospital.facturacion.domain.cita.AgendaCitas
import com.hospital.facturacion.domain.facturador.Facturador
import com.hospital.facturacion.domain.prefactura.ConsecutivoRadicado
import com.hospital.facturacion.domain.prefactura.NumeroDinamica
import com.hospital.facturacion.domain.prefactura.Registro
import com.hospital.facturacion.domain.prefactura.SolicitudPrefacturaRepositorio
import com.hospital.facturacion.domain.prefactura.registrarIngreso
import java.time.Clock

/** Asocia al paciente y a la cita el ID de ingreso que el facturador creó en Dinámica (adaptador manual). */
class RegistrarIngreso(
    private val solicitudes: SolicitudPrefacturaRepositorio,
    private val agenda: AgendaCitas,
    private val reloj: Clock,
) {
    fun ejecutar(consecutivo: ConsecutivoRadicado, numero: String, facturador: Facturador): ResultadoAccion {
        val ingreso = NumeroDinamica.de(numero) ?: return ResultadoAccion.DatoInvalido
        return solicitudes.aplicarElDiaDeLaCita(consecutivo, agenda, reloj) {
            it.registrarIngreso(Registro(ingreso, facturador.usuario, reloj.instant()))
        }
    }
}
