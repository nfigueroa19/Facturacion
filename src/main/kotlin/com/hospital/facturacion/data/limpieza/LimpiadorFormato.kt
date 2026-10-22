package com.hospital.facturacion.data.limpieza

import com.hospital.facturacion.domain.contenido.FormatoImagen

/** Estrategia de limpieza para un formato. Un formato nuevo se agrega con otra implementación. */
interface LimpiadorFormato {
    val formato: FormatoImagen

    /** Copia sin metadatos, o `null` si la estructura del archivo no es válida. */
    fun limpiar(bytes: ByteArray): ByteArray?
}
