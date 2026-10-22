package com.hospital.facturacion.data.contenido

import java.nio.file.attribute.FileTime

interface LectorContenido {
    /** Marca de modificación del origen; `null` si no se puede leer. */
    fun version(): FileTime?

    fun leer(): LecturaContenido
}
