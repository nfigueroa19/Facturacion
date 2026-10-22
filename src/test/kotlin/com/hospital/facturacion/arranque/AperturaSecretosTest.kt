package com.hospital.facturacion.arranque

import com.hospital.facturacion.AlmacenesDePrueba
import org.junit.jupiter.api.io.TempDir
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class AperturaSecretosTest {
    @TempDir
    lateinit var temporal: Path
    private val registro = LoggerFactory.getLogger(javaClass)

    @Test
    fun `sin almacen ni secretos requeridos arranca sin fuente`() {
        assertNull(abrirSecretos(ConfiguracionSecretos(null, emptySet()), { null }, registro))
        assertFailsWith<IllegalStateException> { abrirSecretos(ConfiguracionSecretos(null, setOf("a")), { null }, registro) }
    }

    @Test
    fun `abre el almacen con la clave desde archivo y exige los alias requeridos`() {
        val ruta = AlmacenesDePrueba.crear(temporal.resolve("s.p12"), "clave-prueba".toCharArray(), mapOf("a" to "x"))
        val claveArchivo = Files.writeString(temporal.resolve("clave.txt"), "clave-prueba\n").toString()
        val entorno = mapOf(VARIABLE_CLAVE_ARCHIVO to claveArchivo)::get
        assertEquals(setOf("a"), abrirSecretos(ConfiguracionSecretos(ruta, setOf("a")), entorno, registro)?.aliases())
        assertFailsWith<IllegalStateException> { abrirSecretos(ConfiguracionSecretos(ruta, setOf("a", "b")), entorno, registro) }
    }
}
