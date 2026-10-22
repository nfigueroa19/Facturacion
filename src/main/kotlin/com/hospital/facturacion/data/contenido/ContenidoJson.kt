package com.hospital.facturacion.data.contenido

import com.hospital.facturacion.domain.contenido.ClaveRecurso
import com.hospital.facturacion.domain.contenido.ContenidoInstitucional
import com.hospital.facturacion.domain.contenido.NombreArchivoImagen
import kotlinx.serialization.Serializable

/** Forma de `config/contenido.json`. */
@Serializable
data class ContenidoJson(
    val textos: Map<String, String> = emptyMap(),
    val imagenes: Map<String, String> = emptyMap(),
) {
    fun aDominio(): LecturaContenido {
        val catalogo = mutableMapOf<ClaveRecurso, NombreArchivoImagen>()
        for ((clave, archivo) in imagenes) {
            val claveValida = ClaveRecurso.de(clave)
                ?: return LecturaContenido.Invalida("Clave de imagen no permitida: '$clave'")
            val archivoValido = NombreArchivoImagen.de(archivo)
                ?: return LecturaContenido.Invalida("Nombre de archivo no permitido en '$clave': '$archivo'")
            catalogo[claveValida] = archivoValido
        }
        return LecturaContenido.Valida(ContenidoInstitucional(textos, catalogo))
    }
}
