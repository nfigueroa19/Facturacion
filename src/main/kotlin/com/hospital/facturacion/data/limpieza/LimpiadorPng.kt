package com.hospital.facturacion.data.limpieza

import com.hospital.facturacion.domain.contenido.FormatoImagen
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

/**
 * Conserva solo los chunks que afectan cómo se ve la imagen. Descarta texto (tEXt, iTXt, zTXt),
 * EXIF, C2PA (caBX), fechas y todo lo que venga después de IEND.
 */
class LimpiadorPng : LimpiadorFormato {
    override val formato = FormatoImagen.PNG

    override fun limpiar(bytes: ByteArray): ByteArray? {
        val salida = ByteArrayOutputStream(bytes.size).apply { write(bytes, 0, LONGITUD_FIRMA) }
        val entrada = ByteBuffer.wrap(bytes).position(LONGITUD_FIRMA)
        while (entrada.remaining() >= LONGITUD_MINIMA_CHUNK) {
            val inicio = entrada.position()
            val longitudDatos = entrada.getInt()
            if (longitudDatos < 0 || longitudDatos > entrada.remaining() - 8) return null
            val tipo = String(bytes, inicio + 4, 4, Charsets.ISO_8859_1)
            val longitudChunk = LONGITUD_MINIMA_CHUNK + longitudDatos
            if (tipo in CONSERVADOS) salida.write(bytes, inicio, longitudChunk)
            if (tipo == "IEND") return salida.toByteArray()
            entrada.position(inicio + longitudChunk)
        }
        return null
    }

    private companion object {
        const val LONGITUD_FIRMA = 8
        const val LONGITUD_MINIMA_CHUNK = 12
        val CONSERVADOS = setOf("IHDR", "PLTE", "IDAT", "IEND", "tRNS", "gAMA", "cHRM", "sRGB", "iCCP", "sBIT", "pHYs")
    }
}
