package com.hospital.facturacion

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/** Imitación local de la API de Wompi: responde cada ruta con lo registrado y guarda la cabecera pedida de cada petición. */
class WompiDePrueba(private val cabeceraGuardada: String = "Authorization") : AutoCloseable {
    private val respuestas = ConcurrentHashMap<String, Pair<Int, String>>()
    val cabeceras = CopyOnWriteArrayList<String?>()
    private val servidor = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
        createContext("/") { intercambio ->
            cabeceras += intercambio.requestHeaders.getFirst(cabeceraGuardada)
            val (estado, cuerpo) = respuestas[intercambio.requestURI.path] ?: (404 to """{"error":{"type":"NOT_FOUND_ERROR"}}""")
            val bytes = cuerpo.toByteArray()
            intercambio.sendResponseHeaders(estado, if (bytes.isEmpty()) -1 else bytes.size.toLong())
            intercambio.responseBody.use { it.write(bytes) }
        }
        start()
    }
    val base = "http://127.0.0.1:${servidor.address.port}"

    fun responder(ruta: String, estado: Int, cuerpo: String) {
        respuestas[ruta] = estado to cuerpo
    }

    override fun close() = servidor.stop(0)
}

/** Forma de `GET /v1/merchants/info` en el sandbox, reducida a los contratos de aceptación. */
val COMERCIO_WOMPI = """
    {"data":{"id":1,"presigned_acceptance":{"acceptance_token":"tok-terminos","permalink":"https://wompi.test/reglamento.pdf",
    "type":"END_USER_POLICY"},"presigned_personal_data_auth":{"acceptance_token":"tok-datos",
    "permalink":"https://wompi.test/datos.pdf","type":"PERSONAL_DATA_AUTH"}}}
""".trimIndent()

fun transaccionWompi(id: String, estado: String, referencia: String, montoCentavos: Long = 500_000) = """
    {"data":{"id":"$id","amount_in_cents":$montoCentavos,"reference":"$referencia","currency":"COP",
    "payment_method_type":"CARD","status":"$estado"}}
""".trimIndent()
