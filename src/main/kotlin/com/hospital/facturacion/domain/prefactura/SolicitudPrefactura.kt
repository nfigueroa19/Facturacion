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
    val atencion: Atencion = Atencion(),
) {
    fun registrarNovedad(novedad: Novedad): CambioEstado =
        cambiarEstado(EstadoPrefactura.PENDIENTE) { copy(novedades = novedades + novedad) }

    fun aprobar(validacion: Validacion): CambioEstado = cambiarEstado(EstadoPrefactura.APROBADO) { copy(validacion = validacion) }

    fun corregir(datos: DatosFacturacion, fecha: Instant): CambioEstado =
        cambiarEstado(EstadoPrefactura.EN_REVISION) { copy(datos = datos, fechaEnvio = fecha) }

    /** Aplica la transición del diagrama y el ajuste de datos que la acompaña, o la rechaza sin cambiar nada. */
    fun cambiarEstado(destino: EstadoPrefactura, ajuste: SolicitudPrefactura.() -> SolicitudPrefactura = { this }): CambioEstado =
        if (estado.puedePasarA(destino)) {
            CambioEstado.Aplicado(copy(estado = destino).ajuste())
        } else {
            CambioEstado.Rechazado(actual = estado, destino = destino)
        }
}

/** El paciente puede cambiar datos, soportes y pago antes de radicar o mientras la solicitud tenga novedades. */
fun SolicitudPrefactura?.admiteCambiosDelPaciente(): Boolean =
    this == null || estado.puedePasarA(EstadoPrefactura.EN_REVISION)
