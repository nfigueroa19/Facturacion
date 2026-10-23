package com.hospital.facturacion.data.prefactura

import com.hospital.facturacion.AppwriteDePrueba
import kotlin.test.Test
import kotlin.test.assertEquals

class GeneradorConsecutivoAppwriteTest {
    @Test
    fun `la secuencia continua tras un reinicio porque preparar el contador no lo reinicia`() = AppwriteDePrueba().use { appwrite ->
        val consecutivos = appwrite.tabla("consecutivos", FilaConsecutivo.serializer())
        prepararConsecutivo(consecutivos)
        assertEquals("PF-000001", GeneradorConsecutivoAppwrite(consecutivos).siguiente().valor)
        prepararConsecutivo(consecutivos)
        assertEquals("PF-000002", GeneradorConsecutivoAppwrite(consecutivos).siguiente().valor)
    }
}
