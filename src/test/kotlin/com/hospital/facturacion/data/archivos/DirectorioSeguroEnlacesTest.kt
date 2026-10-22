package com.hospital.facturacion.data.archivos

import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertNull

class DirectorioSeguroEnlacesTest {
    @TempDir
    lateinit var temporal: Path

    // En Windows crear enlaces simbólicos exige modo desarrollador o permisos de administrador.
    @Test
    fun `rechaza un enlace simbolico que apunta fuera de la raiz`() {
        val raiz = Files.createDirectories(temporal.resolve("imagenes"))
        val externo = Files.writeString(temporal.resolve("secreto.png"), "x")
        val creado = runCatching { Files.createSymbolicLink(raiz.resolve("logo.png"), externo) }.isSuccess
        assumeTrue(creado, "El sistema no permite crear enlaces simbólicos")
        assertNull(DirectorioSeguro(raiz).resolver("logo.png"))
    }
}
