package com.hospital.facturacion.domain.formulario

import kotlin.test.Test
import kotlin.test.assertEquals

class TipoCampoTest {
    private val casos = mapOf(
        TipoCampo.FECHA to mapOf("2000-02-29" to true, "2001-02-29" to false, "29/02/2000" to false),
        TipoCampo.CORREO to mapOf("paciente@correo.co" to true, "paciente@correo" to false, "a b@c.co" to false),
        TipoCampo.TELEFONO to mapOf("3001234567" to true, "+57 300 123 4567" to true, "300-123" to false, "abc1234567" to false),
        TipoCampo.TEXTO to mapOf("Cualquier texto" to true),
    )

    @Test
    fun `valida solo el formato tecnico de cada tipo`() {
        casos.forEach { (tipo, valores) ->
            valores.forEach { (valor, esperado) -> assertEquals(esperado, tipo.admite(valor), "$tipo '$valor'") }
        }
    }
}
