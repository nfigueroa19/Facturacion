package com.hospital.facturacion.domain.cita

/** Copago que el call center activa al asignar la cita (RN-09). El origen del valor sigue abierto (P-06). */
@JvmInline
value class Copago(val valorPesos: Long) {
    init {
        require(valorPesos > 0) { "El copago debe ser mayor que cero" }
    }
}
