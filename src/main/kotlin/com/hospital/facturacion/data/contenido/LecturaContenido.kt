package com.hospital.facturacion.data.contenido

import com.hospital.facturacion.domain.contenido.ContenidoInstitucional

sealed interface LecturaContenido {
    data class Valida(val contenido: ContenidoInstitucional) : LecturaContenido

    data class Invalida(val motivo: String) : LecturaContenido
}
