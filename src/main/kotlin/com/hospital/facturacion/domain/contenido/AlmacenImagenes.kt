package com.hospital.facturacion.domain.contenido

fun interface AlmacenImagenes {
    /** `null` si el archivo no existe, supera el tamaño permitido o queda fuera del directorio autorizado. */
    fun leer(nombre: NombreArchivoImagen): ByteArray?
}
