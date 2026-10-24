package com.hospital.facturacion.data.copago

import com.sun.net.httpserver.HttpServer
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.net.http.HttpClient
import kotlin.test.Test
import kotlin.test.assertEquals

class ClienteTransaccionesWompiTest {
    private val aprobada = """
        {"data":{"id":"15113-1729800000-12345","amount_in_cents":500000,"reference":"cita-1_0a0a0a0a0a0a","currency":"COP",
        "payment_method_type":"CARD","status":"APPROVED"}}
    """.trimIndent()

    @Test
    fun `consulta la transaccion con la llave privada y lee estado, referencia, monto y moneda`() = conWompi(200, aprobada) { cliente, autorizaciones ->
        val esperada = TransaccionWompi("15113-1729800000-12345", EstadoTransaccionWompi.APPROVED, "cita-1_0a0a0a0a0a0a", 500_000, "COP")
        assertEquals(ConsultaTransaccion.Encontrada(esperada), cliente.consultar("15113-1729800000-12345"))
        assertEquals(listOf("Bearer prv_test_prueba"), autorizaciones)
    }

    @Test
    fun `un 404 es una transaccion inexistente`() = conWompi(404, """{"error":{"type":"NOT_FOUND_ERROR"}}""") { cliente, _ ->
        assertEquals(ConsultaTransaccion.NoExiste, cliente.consultar("15113-1"))
    }

    @Test
    fun `un estado desconocido, un cuerpo ilegible o un 5xx son una falla de Wompi`() {
        conWompi(200, aprobada.replace("APPROVED", "OTRO")) { cliente, _ -> assertEquals(ConsultaTransaccion.Falla, cliente.consultar("15113-1")) }
        conWompi(200, "{}") { cliente, _ -> assertEquals(ConsultaTransaccion.Falla, cliente.consultar("15113-1")) }
        conWompi(503, "") { cliente, _ -> assertEquals(ConsultaTransaccion.Falla, cliente.consultar("15113-1")) }
    }
}

/** Imitación local de `GET /v1/transactions/{id}` que guarda la cabecera Authorization de cada petición. */
private fun conWompi(estado: Int, respuesta: String, bloque: (ClienteTransaccionesWompi, MutableList<String>) -> Unit) {
    val autorizaciones = mutableListOf<String>()
    val servidor = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
        createContext("/v1/transactions/") { intercambio ->
            autorizaciones += intercambio.requestHeaders.getFirst("Authorization")
            val bytes = respuesta.toByteArray()
            intercambio.sendResponseHeaders(estado, if (bytes.isEmpty()) -1 else bytes.size.toLong())
            intercambio.responseBody.use { it.write(bytes) }
        }
        start()
    }
    val credenciales = CredencialesWompi("pub_test_prueba", "prv_test_prueba", "test_integrity_prueba", "http://127.0.0.1:${servidor.address.port}")
    try {
        bloque(ClienteTransaccionesWompi(credenciales, HttpClient.newHttpClient(), LoggerFactory.getLogger("pruebas")), autorizaciones)
    } finally {
        servidor.stop(0)
    }
}
