package com.hospital.facturacion.data.archivos

import com.hospital.facturacion.domain.contenido.NombreArchivoImagen
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertNull

class AlmacenImagenesArchivoTest {
    @TempDir
    lateinit var raiz: Path

    private fun leer(nombre: String) =
        AlmacenImagenesArchivo(DirectorioSeguro(raiz), tamanoMaximoBytes = 4).leer(requireNotNull(NombreArchivoImagen.de(nombre)))

    @Test
    fun `lee archivos dentro del limite de tamano`() {
        Files.write(raiz.resolve("logo.png"), byteArrayOf(1, 2, 3, 4))
        assertContentEquals(byteArrayOf(1, 2, 3, 4), leer("logo.png"))
    }

    @Test
    fun `no entrega archivos que superan el limite o no existen`() {
        Files.write(raiz.resolve("grande.png"), ByteArray(5))
        assertNull(leer("grande.png"))
        assertNull(leer("falta.png"))
    }
}
