package com.hospital.facturacion.domain.contenido

class ImagenInstitucional private constructor(val formato: FormatoImagen, val contenido: ByteArray) {
    companion object {
        /** Rechaza archivos cuya firma binaria no corresponde al formato declarado por la extensión. */
        fun de(formato: FormatoImagen, contenido: ByteArray): ImagenInstitucional? =
            if (formato.coincideCon(contenido)) ImagenInstitucional(formato, contenido) else null
    }
}
