package com.hospital.facturacion.data.pruebas

import com.hospital.facturacion.domain.soporte.TipoSoporte
import kotlin.test.Test
import kotlin.test.assertTrue

class PdfDePruebaTest {
    @Test
    fun `el PDF de prueba es un PDF valido y cabe en el limite de soportes de las pruebas`() {
        TipoSoporte.entries.map(::pdfDePrueba).forEach { assertTrue(it.contenido.size < 1024) }
    }
}
