package com.hospital.facturacion.domain.formulario

import com.hospital.facturacion.domain.prefactura.DatosFacturacion
import com.hospital.facturacion.formularioDePrueba
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FormularioPrefacturaTest {
    @Test
    fun `recorta valores, descarta opcionales vacios e ignora claves que no estan en el formulario`() {
        val resultado = formularioDePrueba.validar(mapOf("nombre-padre" to "  Padre  ", "correo" to " ", "otra" to "x"))
        assertEquals(ResultadoDatos.Validos(DatosFacturacion(mapOf("nombre-padre" to "Padre"))), resultado)
    }

    @Test
    fun `informa por campo los obligatorios vacios, el formato y el largo`() {
        assertEquals(
            ResultadoDatos.Invalidos(mapOf("nombre-padre" to ErrorCampo.FALTA, "correo" to ErrorCampo.FORMATO)),
            formularioDePrueba.validar(mapOf("correo" to "sin-arroba")),
        )
        val largo = "x".repeat(CampoFormulario.LARGO_MAXIMO + 1)
        assertEquals(ResultadoDatos.Invalidos(mapOf("nombre-padre" to ErrorCampo.LARGO)), formularioDePrueba.validar(mapOf("nombre-padre" to largo)))
    }

    @Test
    fun `no admite formularios vacios ni claves repetidas`() {
        assertFailsWith<IllegalArgumentException> { FormularioPrefactura(emptyList()) }
        val campo = CampoFormulario("eps", "EPS", TipoCampo.TEXTO, obligatorio = true)
        assertFailsWith<IllegalArgumentException> { FormularioPrefactura(listOf(campo, campo)) }
    }
}
