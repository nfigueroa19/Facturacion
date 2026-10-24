package com.hospital.facturacion.service.facturador

import com.hospital.facturacion.domain.cita.AgendaCitas
import com.hospital.facturacion.domain.cita.esHoy
import com.hospital.facturacion.domain.prefactura.CambioEstado
import com.hospital.facturacion.domain.prefactura.ConsecutivoRadicado
import com.hospital.facturacion.domain.prefactura.MotivoRechazoAtencion
import com.hospital.facturacion.domain.prefactura.SolicitudPrefactura
import com.hospital.facturacion.domain.prefactura.SolicitudPrefacturaRepositorio
import java.time.Clock

/** Aplica un cambio de la atención solo el día de la cita (RN-01, RN-03). */
fun SolicitudPrefacturaRepositorio.aplicarElDiaDeLaCita(
    consecutivo: ConsecutivoRadicado,
    agenda: AgendaCitas,
    reloj: Clock,
    cambio: (SolicitudPrefactura) -> CambioEstado,
): ResultadoAccion {
    val solicitud = buscar(consecutivo) ?: return ResultadoAccion.NoEncontrada
    val cita = agenda.buscar(solicitud.cita) ?: return ResultadoAccion.NoEncontrada
    if (!cita.esHoy(reloj)) return ResultadoAccion.ReglaIncumplida(MotivoRechazoAtencion.FUERA_DEL_DIA_DE_LA_CITA)
    return aplicar(cambio(solicitud))
}
