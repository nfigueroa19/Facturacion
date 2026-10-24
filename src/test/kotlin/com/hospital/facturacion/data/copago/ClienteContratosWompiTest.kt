package com.hospital.facturacion.data.copago

import com.hospital.facturacion.COMERCIO_WOMPI
import com.hospital.facturacion.WompiDePrueba
import com.hospital.facturacion.domain.copago.AceptacionPasarela
import com.hospital.facturacion.domain.copago.ContratoPasarela
import org.slf4j.LoggerFactory
import java.net.http.HttpClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ClienteContratosWompiTest {
    private fun consultar(estado: Int, respuesta: String, verificar: (AceptacionPasarela?, List<String?>) -> Unit) =
        WompiDePrueba("x-merchant-public-key").use { wompi ->
            wompi.responder("/v1/merchants/info", estado, respuesta)
            val credenciales = CredencialesWompi("pub_test_prueba", null, wompi.base)
            verificar(ClienteContratosWompi(credenciales, HttpClient.newHttpClient(), LoggerFactory.getLogger("pruebas")).consultar(), wompi.cabeceras)
        }

    @Test
    fun `lee los dos contratos del comercio enviando la llave publica en la cabecera`() = consultar(200, COMERCIO_WOMPI) { aceptacion, cabeceras ->
        val terminos = ContratoPasarela("tok-terminos", "https://wompi.test/reglamento.pdf")
        assertEquals(AceptacionPasarela(terminos, ContratoPasarela("tok-datos", "https://wompi.test/datos.pdf")), aceptacion)
        assertEquals(listOf<String?>("pub_test_prueba"), cabeceras)
    }

    @Test
    fun `sin contratos legibles o con un error de Wompi no hay aceptacion`() {
        consultar(200, """{"data":{"presigned_acceptance":{}}}""") { aceptacion, _ -> assertNull(aceptacion) }
        consultar(401, """{"error":{"type":"INPUT_VALIDATION_ERROR"}}""") { aceptacion, _ -> assertNull(aceptacion) }
    }
}
