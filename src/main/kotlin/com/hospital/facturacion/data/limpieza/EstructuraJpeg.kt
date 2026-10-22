package com.hospital.facturacion.data.limpieza

/**
 * Reglas de segmentos JPEG. De los APPn conserva JFIF (APP0), el perfil de color ICC (APP2)
 * y Adobe (APP14, necesario para decodificar CMYK). Descarta EXIF, XMP, IPTC, C2PA y comentarios.
 */
internal object EstructuraJpeg {
    const val MARCA: Byte = 0xFF.toByte()
    const val EOI = 0xD9
    private const val RELLENO = 0xFF
    private const val SOS = 0xDA
    private const val COM = 0xFE
    private val APP = 0xE0..0xEF
    private val REINICIOS = 0xD0..0xD7
    private val APP_CONSERVADOS = mapOf(0xE0 to "JFIF\u0000", 0xE2 to "ICC_PROFILE\u0000", 0xEE to "Adobe")

    /** Fin del segmento que empieza en [inicio]. Para SOS incluye los datos comprimidos del barrido. */
    fun finSegmento(bytes: ByteArray, inicio: Int, marcador: Int): Int? {
        if (marcador == RELLENO) return inicio + 1
        if (inicio + 4 > bytes.size) return null
        val fin = inicio + 2 + ((bytes.sinSigno(inicio + 2) shl 8) or bytes.sinSigno(inicio + 3))
        if (fin > bytes.size) return null
        return if (marcador == SOS) finBarrido(bytes, fin) else fin
    }

    fun conservar(bytes: ByteArray, inicio: Int, marcador: Int): Boolean = when (marcador) {
        RELLENO, COM -> false
        in APP -> APP_CONSERVADOS[marcador]?.let { firma -> empiezaCon(bytes, inicio + 4, firma) } ?: false
        else -> true
    }

    // Dentro del barrido, FF00 es un byte escapado y FFD0..FFD7 son reinicios; otro FFxx lo cierra.
    private fun finBarrido(bytes: ByteArray, desde: Int): Int = (desde until bytes.size - 1).firstOrNull { i ->
        val siguiente = bytes.sinSigno(i + 1)
        bytes[i] == MARCA && siguiente != 0x00 && siguiente != RELLENO && siguiente !in REINICIOS
    } ?: bytes.size

    private fun empiezaCon(bytes: ByteArray, desde: Int, firma: String): Boolean =
        desde + firma.length <= bytes.size && firma.indices.all { bytes[desde + it] == firma[it].code.toByte() }
}

internal fun ByteArray.sinSigno(indice: Int): Int = this[indice].toInt() and 0xFF
