package com.hospital.facturacion.data.appwrite

import org.slf4j.LoggerFactory
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ReintentosAppwriteTest {
    private val esperas = mutableListOf<Duration>()
    private fun reintentos(intentos: Int) = ReintentosAppwrite(intentos, Duration.ofMillis(250), esperas::add, LoggerFactory.getLogger("pruebas"))

    @Test
    fun `duplica la espera entre intentos y devuelve el primer resultado correcto`() {
        var llamadas = 0
        val resultado = reintentos(4).ejecutar {
            llamadas++
            if (llamadas < 4) throw FalloTransitorioAppwrite("Appwrite respondió 503") else "fila"
        }
        assertEquals("fila", resultado)
        assertEquals(listOf(250L, 500L, 1000L), esperas.map { it.toMillis() })
    }

    @Test
    fun `propaga la ultima falla transitoria y no reintenta las demas excepciones`() {
        var llamadas = 0
        assertFailsWith<FalloTransitorioAppwrite> { reintentos(2).ejecutar { llamadas++; throw FalloTransitorioAppwrite("sin respuesta") } }
        assertEquals(2, llamadas)
        assertFailsWith<IllegalStateException> { reintentos(3).ejecutar { llamadas++; error("Appwrite respondió 400") } }
        assertEquals(3, llamadas)
        assertFailsWith<IllegalArgumentException> { reintentos(0) }
    }
}
