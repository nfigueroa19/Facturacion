package com.hospital.facturacion.data.archivos

import com.hospital.facturacion.domain.contenido.AlmacenImagenes
import com.hospital.facturacion.domain.contenido.NombreArchivoImagen
import java.io.IOException
import java.nio.file.Files
import java.nio.file.LinkOption

class AlmacenImagenesArchivo(
    private val directorio: DirectorioSeguro,
    private val tamanoMaximoBytes: Int,
) : AlmacenImagenes {
    override fun leer(nombre: NombreArchivoImagen): ByteArray? {
        val ruta = directorio.resolver(nombre.valor) ?: return null
        // Lee como máximo un byte de más para detectar archivos que crecieron después de validarlos.
        val bytes = try {
            Files.newInputStream(ruta, LinkOption.NOFOLLOW_LINKS).use { it.readNBytes(tamanoMaximoBytes + 1) }
        } catch (e: IOException) {
            return null
        }
        return bytes.takeIf { it.size <= tamanoMaximoBytes }
    }
}
