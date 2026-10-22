package com.hospital.facturacion.data.limpieza

import com.hospital.facturacion.domain.contenido.ImagenInstitucional
import com.hospital.facturacion.domain.contenido.LimpiadorMetadatos

/** Un formato sin limpiador registrado no se sirve. */
class LimpiadorMetadatosPorFormato(limpiadores: List<LimpiadorFormato>) : LimpiadorMetadatos {
    private val porFormato = limpiadores.associateBy { it.formato }

    override fun limpiar(imagen: ImagenInstitucional): ImagenInstitucional? {
        val limpiador = porFormato[imagen.formato] ?: return null
        val limpio = limpiador.limpiar(imagen.contenido) ?: return null
        return ImagenInstitucional.de(imagen.formato, limpio)
    }
}
