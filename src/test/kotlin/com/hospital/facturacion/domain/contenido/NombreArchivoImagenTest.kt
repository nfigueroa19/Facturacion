package com.hospital.facturacion.domain.contenido

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NombreArchivoImagenTest {
    @Test
    fun `acepta nombres planos con extension de la lista blanca`() {
        assertEquals(FormatoImagen.PNG, NombreArchivoImagen.de("logo-hospital.png")?.formato)
        assertEquals(FormatoImagen.JPEG, NombreArchivoImagen.de("banner-2026.jpeg")?.formato)
        assertEquals(FormatoImagen.JPEG, NombreArchivoImagen.de("firma.jpg")?.formato)
    }

    @Test
    fun `rechaza rutas, nombres reservados y extensiones fuera de la lista blanca`() {
        val intentos = listOf(
            "../logo.png", "..\\logo.png", "sub/logo.png", "/etc/passwd", "C:\\Windows\\win.ini",
            "%2e%2e%2flogo.png", "logo.png:oculto", "logo.png" + Char(0) + ".jpg", "logo.svg", "logo.png.exe",
            "logo.exe.png", "Logo.PNG", "logo .png", "logo.png ", "con.png", "lpt1.jpg", "-logo.png",
            "", ".png", "a".repeat(70) + ".png",
        )
        intentos.forEach { assertNull(NombreArchivoImagen.de(it), it) }
    }
}
