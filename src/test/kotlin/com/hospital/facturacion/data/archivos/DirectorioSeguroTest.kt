package com.hospital.facturacion.data.archivos

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DirectorioSeguroTest {
    @TempDir
    lateinit var temporal: Path
    private lateinit var directorio: DirectorioSeguro

    @BeforeEach
    fun preparar() {
        val raiz = Files.createDirectories(temporal.resolve("imagenes"))
        Files.writeString(raiz.resolve("logo.png"), "x")
        Files.writeString(Files.createDirectories(raiz.resolve("sub")).resolve("otro.png"), "x")
        Files.writeString(temporal.resolve("secreto.txt"), "x")
        directorio = DirectorioSeguro(raiz)
    }

    @Test
    fun `resuelve un archivo regular dentro de la raiz`() {
        assertNotNull(directorio.resolver("logo.png"))
    }

    @Test
    fun `rechaza rutas que salen de la raiz o no son archivos planos`() {
        val absoluta = temporal.resolve("secreto.txt").toString()
        listOf("../secreto.txt", "..\\secreto.txt", "sub/otro.png", "sub", ".", "", absoluta, "logo.png:ads", "logo.png" + Char(0))
            .forEach { assertNull(directorio.resolver(it), it) }
    }
}
