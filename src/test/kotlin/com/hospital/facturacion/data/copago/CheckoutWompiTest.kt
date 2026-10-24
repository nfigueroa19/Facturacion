package com.hospital.facturacion.data.copago

import com.hospital.facturacion.citaConCopago
import com.hospital.facturacion.domain.cita.Copago
import com.hospital.facturacion.domain.copago.IntencionPago
import java.security.SecureRandom
import kotlin.test.Test
import kotlin.test.assertEquals

class CheckoutWompiTest {
    @Test
    fun `firma referencia, monto en centavos, moneda y secreto de integridad con SHA-256`() {
        val fijo = object : SecureRandom() {
            override fun nextBytes(bytes: ByteArray) = bytes.fill(0x0a)
        }
        val credenciales = CredencialesWompi("pub_test_prueba", "prv_test_prueba", "test_integrity_prueba")
        val firma = "59c0433b77352f8cf76ba7a7291d83617365f48141cf57db7e1176b2c5ddf850"
        val esperada = IntencionPago.Checkout("pub_test_prueba", "cita-1_0a0a0a0a0a0a", 500_000, "COP", firma)
        assertEquals(esperada, CheckoutWompi(credenciales, fijo).preparar(citaConCopago, Copago(5000)))
    }
}
