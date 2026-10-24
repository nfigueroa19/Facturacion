package com.hospital.facturacion.data.copago

import com.hospital.facturacion.citaConCopago
import com.hospital.facturacion.domain.cita.Copago
import com.hospital.facturacion.domain.copago.AceptacionPasarela
import com.hospital.facturacion.domain.copago.ContratoPasarela
import com.hospital.facturacion.domain.copago.IntencionPago
import java.security.SecureRandom
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CheckoutWompiTest {
    private val fijo = object : SecureRandom() {
        override fun nextBytes(bytes: ByteArray) = bytes.fill(0x0a)
    }
    private val aceptacion = AceptacionPasarela(ContratoPasarela("tok-1", "https://wompi.test/1.pdf"), ContratoPasarela("tok-2", "https://wompi.test/2.pdf"))

    private fun preparar(integridad: String?, contratos: ContratosWompi = ContratosWompi { aceptacion }) =
        CheckoutWompi(CredencialesWompi("pub_test_prueba", integridad, "https://wompi.test"), contratos, fijo).preparar(citaConCopago, Copago(5000))

    @Test
    fun `firma referencia, monto en centavos, moneda y secreto de integridad con SHA-256`() {
        val firma = "59c0433b77352f8cf76ba7a7291d83617365f48141cf57db7e1176b2c5ddf850"
        val esperada = IntencionPago("pub_test_prueba", "cita-1_0a0a0a0a0a0a", 500_000, "COP", firma, aceptacion)
        assertEquals(esperada, preparar("test_integrity_prueba"))
    }

    @Test
    fun `sin secreto de integridad no firma y sin contratos de Wompi no prepara el cobro`() {
        assertNull(preparar(null)?.firma)
        assertEquals("cita-1_0a0a0a0a0a0a", preparar(null)?.referencia)
        assertNull(preparar("test_integrity_prueba", ContratosWompi { null }))
    }
}
