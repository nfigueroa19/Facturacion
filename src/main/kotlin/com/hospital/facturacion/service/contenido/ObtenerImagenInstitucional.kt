package com.hospital.facturacion.service.contenido

import com.hospital.facturacion.domain.contenido.AlmacenImagenes
import com.hospital.facturacion.domain.contenido.ClaveRecurso
import com.hospital.facturacion.domain.contenido.FuenteContenido
import com.hospital.facturacion.domain.contenido.ImagenInstitucional
import com.hospital.facturacion.domain.contenido.LimpiadorMetadatos

class ObtenerImagenInstitucional(
    private val fuente: FuenteContenido,
    private val almacen: AlmacenImagenes,
    private val limpiador: LimpiadorMetadatos,
) {
    fun ejecutar(clave: ClaveRecurso): ResultadoImagen {
        val nombre = fuente.actual().imagenes[clave] ?: return ResultadoImagen.NoRegistrada
        val bytes = almacen.leer(nombre) ?: return ResultadoImagen.NoDisponible
        val imagen = ImagenInstitucional.de(nombre.formato, bytes) ?: return ResultadoImagen.Rechazada
        val limpia = limpiador.limpiar(imagen) ?: return ResultadoImagen.Rechazada
        return ResultadoImagen.Encontrada(limpia)
    }
}
