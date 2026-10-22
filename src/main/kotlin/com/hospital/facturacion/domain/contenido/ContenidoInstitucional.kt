package com.hospital.facturacion.domain.contenido

/** Textos y catálogo de imágenes que el personal administrativo edita sin recompilar. */
data class ContenidoInstitucional(
    val textos: Map<String, String>,
    val imagenes: Map<ClaveRecurso, NombreArchivoImagen>,
)
