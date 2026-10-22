package com.hospital.facturacion.data.limpieza

import com.hospital.facturacion.domain.contenido.FormatoImagen
import java.io.ByteArrayOutputStream

/** Recorre los segmentos del JPEG y copia solo los que [EstructuraJpeg.conservar] acepta. */
class LimpiadorJpeg : LimpiadorFormato {
    override val formato = FormatoImagen.JPEG

    override fun limpiar(bytes: ByteArray): ByteArray? {
        val salida = ByteArrayOutputStream(bytes.size).apply { write(bytes, 0, 2) }
        var inicio = 2
        while (inicio + 1 < bytes.size && bytes[inicio] == EstructuraJpeg.MARCA) {
            val marcador = bytes.sinSigno(inicio + 1)
            if (marcador == EstructuraJpeg.EOI) return salida.apply { write(bytes, inicio, 2) }.toByteArray()
            val fin = EstructuraJpeg.finSegmento(bytes, inicio, marcador) ?: return null
            if (EstructuraJpeg.conservar(bytes, inicio, marcador)) salida.write(bytes, inicio, fin - inicio)
            inicio = fin
        }
        return null
    }
}
