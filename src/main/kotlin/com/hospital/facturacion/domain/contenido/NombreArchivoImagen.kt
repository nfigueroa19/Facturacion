package com.hospital.facturacion.domain.contenido

/**
 * Nombre de archivo plano: una [ClaveRecurso] y una sola extensión de la lista blanca.
 * Sin separadores, puntos extra, espacios ni dos puntos, así que no puede expresar una ruta.
 */
class NombreArchivoImagen private constructor(val valor: String, val formato: FormatoImagen) {
    override fun toString(): String = valor

    companion object {
        private val RESERVADOS_WINDOWS = Regex("con|prn|aux|nul|com[0-9]|lpt[0-9]")

        fun de(texto: String): NombreArchivoImagen? {
            val base = texto.substringBeforeLast('.', missingDelimiterValue = "")
            val extension = texto.substringAfterLast('.')
            if (ClaveRecurso.de(base) == null || RESERVADOS_WINDOWS.matches(base)) return null
            val formato = FormatoImagen.porExtension(extension) ?: return null
            return NombreArchivoImagen(texto, formato)
        }
    }
}
