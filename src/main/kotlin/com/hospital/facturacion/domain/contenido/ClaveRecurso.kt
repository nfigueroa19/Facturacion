package com.hospital.facturacion.domain.contenido

/**
 * Nombre lógico de una imagen institucional (por ejemplo `logo-encabezado`).
 * El cliente solo envía claves; nunca nombres de archivo ni rutas.
 */
@JvmInline
value class ClaveRecurso private constructor(val valor: String) {
    companion object {
        private const val LONGITUD_MAXIMA = 64
        private val PATRON = Regex("[a-z0-9]+(-[a-z0-9]+)*")

        fun de(texto: String): ClaveRecurso? =
            texto.takeIf { it.length <= LONGITUD_MAXIMA && PATRON.matches(it) }?.let(::ClaveRecurso)
    }
}
