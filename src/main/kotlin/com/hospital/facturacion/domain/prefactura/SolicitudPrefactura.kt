package com.hospital.facturacion.domain.prefactura

import com.hospital.facturacion.domain.cita.CitaId
import java.time.Instant

data class SolicitudPrefactura(
    val consecutivo: ConsecutivoRadicado,
    val cita: CitaId,
    val estado: EstadoPrefactura,
    val fechaEnvio: Instant,
    val datos: DatosFacturacion,
    val novedades: List<Novedad> = emptyList(),
    val validacion: Validacion? = null,
) {
    fun cambiarEstado(destino: EstadoPrefactura): CambioEstado = pasarA(destino) { this }

    fun registrarNovedad(novedad: Novedad): CambioEstado =
        pasarA(EstadoPrefactura.PENDIENTE) { copy(novedades = novedades + novedad) }

    fun aprobar(validacion: Validacion): CambioEstado = pasarA(EstadoPrefactura.APROBADO) { copy(validacion = validacion) }

    fun corregir(datos: DatosFacturacion, fecha: Instant): CambioEstado =
        pasarA(EstadoPrefactura.EN_REVISION) { copy(datos = datos, fechaEnvio = fecha) }

    private fun pasarA(destino: EstadoPrefactura, ajuste: SolicitudPrefactura.() -> SolicitudPrefactura): CambioEstado =
        if (estado.puedePasarA(destino)) {
            CambioEstado.Aplicado(copy(estado = destino).ajuste())
        } else {
            CambioEstado.Rechazado(actual = estado, destino = destino)
        }
}

/** El paciente puede cambiar datos, soportes y pago antes de radicar o mientras la solicitud tenga novedades. */
fun SolicitudPrefactura?.admiteCambiosDelPaciente(): Boolean =
    this == null || estado.puedePasarA(EstadoPrefactura.EN_REVISION)
