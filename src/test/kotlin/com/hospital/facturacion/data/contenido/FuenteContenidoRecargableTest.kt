package com.hospital.facturacion.data.contenido

import com.hospital.facturacion.domain.contenido.ContenidoInstitucional
import org.slf4j.LoggerFactory
import java.nio.file.attribute.FileTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FuenteContenidoRecargableTest {
    private class LectorFalso(var marca: Long, var lectura: LecturaContenido) : LectorContenido {
        override fun version(): FileTime = FileTime.fromMillis(marca)
        override fun leer(): LecturaContenido = lectura
    }

    private fun valida(titulo: String) = LecturaContenido.Valida(ContenidoInstitucional(mapOf("titulo" to titulo), emptyMap()))
    private val registro = LoggerFactory.getLogger(javaClass)

    @Test
    fun `recarga al cambiar la version y conserva la ultima valida si la edicion es invalida`() {
        val lector = LectorFalso(1, valida("v1"))
        val fuente = FuenteContenidoRecargable(lector, registro)
        lector.apply { marca = 2; lectura = valida("v2") }
        assertEquals("v2", fuente.actual().textos["titulo"])
        lector.apply { marca = 3; lectura = LecturaContenido.Invalida("falta una coma") }
        assertEquals("v2", fuente.actual().textos["titulo"])
    }

    @Test
    fun `no arranca con contenido invalido`() {
        assertFailsWith<IllegalStateException> { FuenteContenidoRecargable(LectorFalso(1, LecturaContenido.Invalida("x")), registro) }
    }
}
