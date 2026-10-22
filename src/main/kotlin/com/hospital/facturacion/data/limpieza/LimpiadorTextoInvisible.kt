package com.hospital.facturacion.data.limpieza

import com.hospital.facturacion.domain.contenido.LimpiadorTexto

/**
 * Capa A de guillaumemeyer/watermarks-remover (MIT): elimina caracteres invisibles, de control
 * bidireccional, etiquetas Unicode, selectores de variación y uso privado, y normaliza espacios
 * equivalentes a U+0020. A diferencia del original, no preserva ZWJ ni selectores dentro de emojis.
 */
class LimpiadorTextoInvisible : LimpiadorTexto {
    override fun limpiar(texto: String): String = buildString(texto.length) {
        texto.codePoints().forEach { punto ->
            when {
                ESPACIOS_EQUIVALENTES.any { punto in it } -> append(' ')
                !esInvisible(punto) -> appendCodePoint(punto)
            }
        }
    }

    private fun esInvisible(punto: Int): Boolean =
        ELIMINADOS.any { punto in it } || punto in 0xFDD0..0xFDEF || (punto and 0xFFFE) == 0xFFFE

    private companion object {
        val ESPACIOS_EQUIVALENTES = listOf(0x00A0..0x00A0, 0x1680..0x1680, 0x2000..0x200A, 0x202F..0x202F, 0x205F..0x205F, 0x3000..0x3000)
        val ELIMINADOS = listOf(
            0x00AD..0x00AD, 0x034F..0x034F, 0x061C..0x061C, 0x115F..0x1160, 0x17B4..0x17B5, 0x180B..0x180F,
            0x200B..0x200F, 0x202A..0x202E, 0x2060..0x206F, 0x3164..0x3164, 0xFE00..0xFE0F, 0xFEFF..0xFEFF,
            0xFFA0..0xFFA0, 0xFFF0..0xFFFB, 0xE000..0xF8FF, 0xE0000..0xE0FFF, 0xF0000..0x10FFFF,
        )
    }
}
