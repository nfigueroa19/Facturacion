package com.hospital.facturacion.domain.prefactura

// Pasos del diagrama del facturador después de la aprobación. La regla del día de la cita (RN-01, RN-03) la aplica el caso de uso.

/** Ingreso creado en Dinámica y asociado al paciente y a la cita. Se registra una sola vez, antes o después de la llegada. */
fun SolicitudPrefactura.registrarIngreso(ingreso: Registro<NumeroDinamica>): CambioEstado = when {
    estado != EstadoPrefactura.APROBADO && estado != EstadoPrefactura.LISTO_PARA_FACTURAR ->
        CambioEstado.Incumple(MotivoRechazoAtencion.ESTADO_NO_PERMITE)
    atencion.ingreso != null -> CambioEstado.Incumple(MotivoRechazoAtencion.INGRESO_YA_REGISTRADO)
    else -> CambioEstado.Aplicado(copy(atencion = atencion.copy(ingreso = ingreso)))
}

/** El paciente no llegó: se anula el ID de ingreso (RN-13). Las reglas institucionales de anulación siguen abiertas (P-03). */
fun SolicitudPrefactura.anularIngreso(motivo: Registro<Observacion>): CambioEstado =
    conIngreso(EstadoPrefactura.INGRESO_ANULADO) { copy(atencion = atencion.copy(anulacion = motivo)) }

/** Facturación completada en Dinámica: queda el número de factura. */
fun SolicitudPrefactura.registrarFactura(factura: Registro<NumeroDinamica>): CambioEstado =
    conIngreso(EstadoPrefactura.FACTURADO) { copy(atencion = atencion.copy(factura = factura)) }

/** Dinámica no completó la facturación: el error queda registrado y la solicitud sigue lista para reintentar. */
fun SolicitudPrefactura.registrarIntentoFallido(error: Registro<Observacion>): CambioEstado = when {
    estado != EstadoPrefactura.LISTO_PARA_FACTURAR -> CambioEstado.Incumple(MotivoRechazoAtencion.ESTADO_NO_PERMITE)
    atencion.ingreso == null -> CambioEstado.Incumple(MotivoRechazoAtencion.SIN_INGRESO)
    else -> CambioEstado.Aplicado(copy(atencion = atencion.copy(intentosFallidos = atencion.intentosFallidos + error)))
}

private fun SolicitudPrefactura.conIngreso(destino: EstadoPrefactura, ajuste: SolicitudPrefactura.() -> SolicitudPrefactura) = when {
    !estado.puedePasarA(destino) -> CambioEstado.Rechazado(estado, destino)
    atencion.ingreso == null -> CambioEstado.Incumple(MotivoRechazoAtencion.SIN_INGRESO)
    else -> cambiarEstado(destino, ajuste)
}
