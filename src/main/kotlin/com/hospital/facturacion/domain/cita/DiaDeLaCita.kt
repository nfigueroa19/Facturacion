package com.hospital.facturacion.domain.cita

import java.time.Clock
import java.time.LocalDate

/** El ingreso se abre y la factura se genera solo el mismo día de la cita, a cualquier hora (RN-01, RN-03). */
fun Cita.esHoy(reloj: Clock): Boolean = LocalDate.now(reloj) == fecha
