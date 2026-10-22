package com.hospital.facturacion.domain.prefactura

import java.time.Instant

/** Registro de la aprobación con checklist [12:43]. */
data class Validacion(val facturador: String, val fecha: Instant)
