package com.hospital.facturacion.domain.cita

/** Identificador de la cita asignada por el call center. También forma el nombre de archivo de sus soportes. */
@JvmInline
value class CitaId private constructor(val valor: String) {
    companion object {
        private val PATRON = Regex("[a-z0-9]+(-[a-z0-9]+)*")

        fun de(valor: String): CitaId? = valor.takeIf { it.length <= 40 && PATRON.matches(it) }?.let(::CitaId)
    }
}
