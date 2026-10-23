package com.hospital.facturacion.data.prefactura

import com.hospital.facturacion.PostgresDePrueba
import kotlin.test.Test
import kotlin.test.assertEquals

class GeneradorConsecutivoPostgresTest {
    @Test
    fun `la secuencia continua aunque se cree otro generador, como tras un reinicio`() {
        val base = PostgresDePrueba.baseMigrada()
        assertEquals("PF-000001", GeneradorConsecutivoPostgres(base).siguiente().valor)
        assertEquals("PF-000002", GeneradorConsecutivoPostgres(base).siguiente().valor)
    }
}
