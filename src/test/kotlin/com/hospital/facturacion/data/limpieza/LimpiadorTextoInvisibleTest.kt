package com.hospital.facturacion.data.limpieza

import kotlin.test.Test
import kotlin.test.assertEquals

class LimpiadorTextoInvisibleTest {
    private val limpiador = LimpiadorTextoInvisible()

    private fun puntos(vararg codigos: Int) = codigos.joinToString("") { Character.toString(it) }

    @Test
    fun `elimina caracteres invisibles y normaliza espacios equivalentes`() {
        val sucio = "Hola" + puntos(0x200B) + " mundo" + puntos(0x00A0) + "fin" + puntos(0xE0041, 0xFEFF, 0x202E, 0xFE0F)
        assertEquals("Hola mundo fin", limpiador.limpiar(sucio))
    }

    @Test
    fun `conserva tildes, enes y signos de apertura del espanol`() {
        val texto = "¿Número de identificación? ¡Sí, señora Muñoz!"
        assertEquals(texto, limpiador.limpiar(texto))
    }
}
