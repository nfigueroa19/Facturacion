package com.hospital.facturacion.data.archivos

import java.io.IOException
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.LinkOption
import java.nio.file.Path

/**
 * Directorio raíz autorizado (sandbox) para leer archivos. Solo resuelve archivos regulares que estén
 * directamente dentro de la raíz: rechaza `..`, rutas absolutas, subdirectorios, enlaces simbólicos
 * y uniones de Windows que apunten fuera, nombres con caracteres no válidos y flujos alternos (`:`).
 */
class DirectorioSeguro(raiz: Path) {
    private val raiz: Path = raiz.toRealPath()

    fun resolver(nombre: String): Path? {
        val candidato = candidato(nombre) ?: return null
        if (!Files.isRegularFile(candidato, LinkOption.NOFOLLOW_LINKS)) return null
        return try {
            candidato.takeIf { it.toRealPath().startsWith(raiz) }
        } catch (e: IOException) {
            null
        }
    }

    /** Ruta para crear o reemplazar un archivo. Las mismas reglas de [resolver], pero el archivo puede no existir. */
    fun destino(nombre: String): Path? =
        candidato(nombre)?.takeIf { Files.notExists(it, LinkOption.NOFOLLOW_LINKS) || resolver(nombre) != null }

    private fun candidato(nombre: String): Path? {
        val ruta = try {
            raiz.resolve(nombre).normalize()
        } catch (e: InvalidPathException) {
            return null
        }
        return ruta.takeIf { it.parent == raiz }
    }
}
