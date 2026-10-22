package com.hospital.facturacion.service.contenido

import com.hospital.facturacion.domain.contenido.ImagenInstitucional

sealed interface ResultadoImagen {
    data class Encontrada(val imagen: ImagenInstitucional) : ResultadoImagen

    /** La clave no está en el catálogo de `config/contenido.json`. */
    data object NoRegistrada : ResultadoImagen

    /** El archivo falta, supera el tamaño permitido o quedó fuera del directorio autorizado. */
    data object NoDisponible : ResultadoImagen

    /** El contenido no corresponde a la extensión o no se pudo limpiar. */
    data object Rechazada : ResultadoImagen
}
