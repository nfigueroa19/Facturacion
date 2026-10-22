package com.hospital.facturacion.data.archivos

import com.hospital.facturacion.citaIdDePrueba
import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.soporte.TipoSoporte
import com.hospital.facturacion.pdfDePrueba
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AlmacenSoportesArchivoTest {
    @TempDir
    lateinit var raiz: Path

    private fun almacen(maximo: Int = 1024) = AlmacenSoportesArchivo(DirectorioSeguro(raiz), maximo)

    @Test
    fun `guarda cada soporte con el nombre armado por el servidor y lo reemplaza al cargar de nuevo`() {
        almacen().guardar(citaIdDePrueba, TipoSoporte.ORDEN_MEDICA, pdfDePrueba("%PDF-1 primera"))
        almacen().guardar(citaIdDePrueba, TipoSoporte.ORDEN_MEDICA, pdfDePrueba("%PDF-1 segunda"))
        assertEquals(listOf("cita-1-orden-medica.pdf"), raiz.listDirectoryEntries().map { it.fileName.toString() })
        assertEquals("%PDF-1 segunda", almacen().leer(citaIdDePrueba, TipoSoporte.ORDEN_MEDICA)?.contenido?.decodeToString())
        assertEquals(setOf(TipoSoporte.ORDEN_MEDICA), almacen().cargados(citaIdDePrueba))
        assertEquals(emptySet(), almacen().cargados(requireNotNull(CitaId.de("otra"))))
    }

    @Test
    fun `no entrega archivos alterados en disco que ya no son PDF o superan el limite`() {
        Files.writeString(raiz.resolve("cita-1-autorizacion.pdf"), "<html>")
        assertNull(almacen().leer(citaIdDePrueba, TipoSoporte.AUTORIZACION))
        almacen().guardar(citaIdDePrueba, TipoSoporte.ORDEN_MEDICA, pdfDePrueba("%PDF-1 grande"))
        assertNull(almacen(maximo = 4).leer(citaIdDePrueba, TipoSoporte.ORDEN_MEDICA))
    }

    @Test
    fun `el destino de escritura aplica las mismas reglas que la lectura`() {
        val directorio = DirectorioSeguro(raiz)
        assertEquals(raiz.toRealPath().resolve("nuevo.pdf"), directorio.destino("nuevo.pdf"))
        listOf("../fuera.pdf", "sub/x.pdf", "..", raiz.resolve("x.pdf").toAbsolutePath().parent.parent.resolve("y.pdf").toString())
            .forEach { assertNull(directorio.destino(it), it) }
        Files.createDirectory(raiz.resolve("carpeta"))
        assertNull(directorio.destino("carpeta"))
    }
}
