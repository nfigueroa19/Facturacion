package com.hospital.facturacion.domain.contenido

fun interface LimpiadorMetadatos {
    /** `null` si la imagen no se pudo interpretar; en ese caso no se sirve. */
    fun limpiar(imagen: ImagenInstitucional): ImagenInstitucional?
}
