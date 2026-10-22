package com.hospital.facturacion.domain.contenido

/** Lista blanca de formatos de imagen. SVG queda fuera porque puede ejecutar scripts. */
enum class FormatoImagen(val tipoMime: String, val extensiones: Set<String>, private val firma: ByteArray) {
    PNG("image/png", setOf("png"), byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)),
    JPEG("image/jpeg", setOf("jpg", "jpeg"), byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte()));

    fun coincideCon(contenido: ByteArray): Boolean =
        contenido.size >= firma.size && firma.indices.all { contenido[it] == firma[it] }

    companion object {
        fun porExtension(extension: String): FormatoImagen? = entries.firstOrNull { extension in it.extensiones }
    }
}
