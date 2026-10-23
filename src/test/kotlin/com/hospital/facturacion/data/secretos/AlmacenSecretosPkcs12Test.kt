package com.hospital.facturacion.data.secretos

import com.hospital.facturacion.AlmacenesDePrueba
import org.junit.jupiter.api.io.TempDir
import java.io.IOException
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AlmacenSecretosPkcs12Test {
    @TempDir
    lateinit var temporal: Path

    private fun almacen() = AlmacenesDePrueba.crear(temporal.resolve("s.p12"), "clave-prueba".toCharArray(), mapOf("basedatos.clave" to "valor-ficticio"))

    @Test
    fun `lee un secreto por alias sin exponerlo en toString`() {
        val secreto = assertNotNull(AlmacenSecretosPkcs12(almacen(), "clave-prueba".toCharArray()).leer("basedatos.clave"))
        assertEquals("valor-ficticio", secreto.usar { String(it) })
        assertEquals("Secreto(****)", secreto.toString())
        assertNull(AlmacenSecretosPkcs12(almacen(), "clave-prueba".toCharArray()).leer("no-existe"))
    }

    @Test
    fun `falla con una clave incorrecta`() {
        assertFailsWith<IOException> { AlmacenSecretosPkcs12(almacen(), "otra".toCharArray()) }
    }
}
