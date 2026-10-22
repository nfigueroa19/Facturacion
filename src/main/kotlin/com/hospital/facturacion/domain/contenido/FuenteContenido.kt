package com.hospital.facturacion.domain.contenido

fun interface FuenteContenido {
    /** Última versión válida del contenido; refleja los cambios del archivo sin reiniciar. */
    fun actual(): ContenidoInstitucional
}
