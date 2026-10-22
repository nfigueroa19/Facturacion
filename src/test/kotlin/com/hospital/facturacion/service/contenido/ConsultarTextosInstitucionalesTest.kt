package com.hospital.facturacion.service.contenido

import com.hospital.facturacion.domain.contenido.ContenidoInstitucional
import com.hospital.facturacion.domain.contenido.FuenteContenido
import kotlin.test.Test
import kotlin.test.assertEquals

class ConsultarTextosInstitucionalesTest {
    @Test
    fun `aplica el limpiador a cada texto`() {
        val fuente = FuenteContenido { ContenidoInstitucional(mapOf("titulo" to "sucio"), emptyMap()) }
        val caso = ConsultarTextosInstitucionales(fuente) { it.replace("sucio", "limpio") }
        assertEquals(mapOf("titulo" to "limpio"), caso.ejecutar())
    }
}
