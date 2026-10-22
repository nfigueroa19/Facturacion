package com.hospital.facturacion.domain.contenido

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ClaveRecursoTest {
    @Test
    fun `acepta claves en minusculas con guiones`() {
        assertEquals("logo-encabezado", ClaveRecurso.de("logo-encabezado")?.valor)
    }

    @Test
    fun `rechaza claves que podrian interpretarse como rutas`() {
        listOf("../logo", "logo.png", "Logo", "logo--x", "-logo", "logo/x", "", "a".repeat(65))
            .forEach { assertNull(ClaveRecurso.de(it), it) }
    }
}
