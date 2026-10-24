package com.hospital.facturacion.arranque

import com.hospital.facturacion.data.copago.CheckoutWompi
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

    private fun abrir(llave: String = "pub_test_prueba", secretos: Map<String, String>? = null) = abrirPasarelaCopago(
        ConfiguracionPagos("https://sandbox.wompi.co", llave),
        secretos?.let(::FuenteEnMemoria),
        HttpClient.newHttpClient(),
        SecureRandom(),
        registro,
    )

    @Test
    fun `usa Wompi sin almacen de secretos y con el secreto de integridad opcional`() {
        assertIs<CheckoutWompi>(abrir().checkout)
        assertIs<PasarelaWompi>(abrir(secretos = mapOf(ALIAS_WOMPI_INTEGRIDAD to "test_integrity_prueba")).cobro)
    }

    @Test
    fun `detiene el arranque con llaves de produccion`() {
        assertFailsWith<IllegalStateException> { abrir("pub_prod_prueba") }
        assertFailsWith<IllegalStateException> { abrir(secretos = mapOf(ALIAS_WOMPI_INTEGRIDAD to "prod_integrity_prueba")) }
    }
}

private class FuenteEnMemoria(private val valores: Map<String, String>) : FuenteSecretos {
    override fun aliases() = valores.keys

    override fun leer(alias: String) = valores[alias]?.let { Secreto(it.toCharArray()) }
}
