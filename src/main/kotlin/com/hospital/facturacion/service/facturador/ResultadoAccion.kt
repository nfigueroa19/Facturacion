package com.hospital.facturacion.service.facturador

import com.hospital.facturacion.domain.prefactura.CambioEstado
import com.hospital.facturacion.domain.prefactura.EstadoPrefactura
import com.hospital.facturacion.domain.prefactura.ItemRevision
import com.hospital.facturacion.domain.prefactura.SolicitudPrefactura
import com.hospital.facturacion.domain.prefactura.SolicitudPrefacturaRepositorio

sealed interface ResultadoAccion {
    data class Aplicada(val solicitud: SolicitudPrefactura) : ResultadoAccion

    data object NoEncontrada : ResultadoAccion

    data class TransicionInvalida(val actual: EstadoPrefactura, val destino: EstadoPrefactura) : ResultadoAccion

    data class ChecklistIncompleto(val faltantes: Set<ItemRevision>) : ResultadoAccion

    data object NovedadInvalida : ResultadoAccion
}

/** Guarda la solicitud solo si el dominio aceptó la transición. */
fun SolicitudPrefacturaRepositorio.aplicar(cambio: CambioEstado): ResultadoAccion = when (cambio) {
    is CambioEstado.Aplicado -> ResultadoAccion.Aplicada(cambio.solicitud).also { guardar(cambio.solicitud) }
    is CambioEstado.Rechazado -> ResultadoAccion.TransicionInvalida(cambio.actual, cambio.destino)
}
