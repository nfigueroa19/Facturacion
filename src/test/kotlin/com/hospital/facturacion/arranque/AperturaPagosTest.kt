package com.hospital.facturacion.arranque

import com.hospital.facturacion.data.copago.CheckoutWompi
import com.hospital.facturacion.data.copago.PasarelaPagosSimulada
import com.hospital.facturacion.data.copago.PasarelaWompi
import com.hospital.facturacion.data.secretos.FuenteSecretos
import com.hospital.facturacion.data.secretos.Secreto
import org.slf4j.LoggerFactory
import java.net.http.HttpClient
import java.security.SecureRandom
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class AperturaPagosTest {
    private val registro = LoggerFactory.getLogger(javaClass)
    private val llavesPrueba = mapOf(
        ALIAS_WOMPI_PUBLICA to "pub_test_prueba",
        ALIAS_WOMPI_PRIVADA to "prv_test_prueba",
        ALIAS_WOMPI_INTEGRIDAD to "test_integrity_prueba",
    )

    private fun abrir(valores: Map<String, String>?) =
        abrirPasarelaCopago(valores?.let(::FuenteEnMemoria), HttpClient.newHttpClient(), SecureRandom(), registro)

    @Test
    fun `usa Wompi con las tres llaves de pruebas y la pasarela simulada si falta alguna`() {
        val wompi = abrir(llavesPrueba)
        assertIs<CheckoutWompi>(wompi.checkout)
        assertIs<PasarelaWompi>(wompi.cobro)
        assertIs<PasarelaPagosSimulada>(abrir(llavesPrueba - ALIAS_WOMPI_INTEGRIDAD).cobro)
        assertIs<PasarelaPagosSimulada>(abrir(null).checkout)
    }

    @Test
    fun `detiene el arranque con llaves de produccion`() {
        assertFailsWith<IllegalStateException> { abrir(llavesPrueba + (ALIAS_WOMPI_PUBLICA to "pub_prod_prueba")) }
    }
}

private class FuenteEnMemoria(private val valores: Map<String, String>) : FuenteSecretos {
    override fun aliases() = valores.keys

    override fun leer(alias: String) = valores[alias]?.let { Secreto(it.toCharArray()) }
}
