package com.hospital.facturacion.data.comunicacion

import com.hospital.facturacion.domain.comunicacion.Celular
import com.hospital.facturacion.domain.comunicacion.EstadoEnvio
import com.hospital.facturacion.domain.comunicacion.MensajeSms
import com.hospital.facturacion.domain.comunicacion.ResultadoEnvio
import com.sun.net.httpserver.HttpServer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.net.http.HttpClient
import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertEquals

class CanalSmsLabsMobileTest {
    private val mensaje = MensajeSms(requireNotNull(Celular.de("3000000009")), "Su identificación y el ID de acceso ABCD2345")

    @Test
    fun `envia con autenticacion basica, indicativo 57, mensaje largo y unicode por las tildes`() = conLabsMobile("""{"code":"0"}""", simular = false) { canal, recibidas ->
        assertEquals(ResultadoEnvio(EstadoEnvio.ENVIADO, "labsmobile"), canal.enviar(mensaje))
        val (autorizacion, cuerpo) = recibidas.single()
        assertEquals("Basic " + Base64.getEncoder().encodeToString("prueba@hospital.test:token-secreto".toByteArray()), autorizacion)
        val esperado = """{"message":"Su identificación y el ID de acceso ABCD2345","recipient":[{"msisdn":"573000000009"}],"long":1,"ucs2":1}"""
        assertEquals(Json.parseToJsonElement(esperado), cuerpo)
    }

    @Test
    fun `en modo de prueba pide test y el SMS queda simulado`() = conLabsMobile("""{"code":"0"}""", simular = true) { canal, recibidas ->
        assertEquals(ResultadoEnvio(EstadoEnvio.SIMULADO, "labsmobile"), canal.enviar(mensaje))
        assertEquals("1", recibidas.single().second["test"].toString())
    }

    @Test
    fun `un codigo distinto de cero, como saldo insuficiente, queda como envio fallido`() = conLabsMobile("""{"code":"35"}""", simular = false) { canal, _ ->
        assertEquals(EstadoEnvio.FALLIDO, canal.enviar(mensaje).estado)
    }
}

/** Imitación local de LabsMobile que responde `respuesta` y guarda la autorización y el cuerpo JSON de cada petición. */
private fun conLabsMobile(respuesta: String, simular: Boolean, bloque: (CanalSmsLabsMobile, MutableList<Pair<String, JsonObject>>) -> Unit) {
    val recibidas = mutableListOf<Pair<String, JsonObject>>()
    val servidor = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
        createContext("/json/send") { intercambio ->
            val cuerpo = Json.parseToJsonElement(intercambio.requestBody.readAllBytes().decodeToString()).jsonObject
            recibidas += intercambio.requestHeaders.getFirst("Authorization") to cuerpo
            val bytes = respuesta.toByteArray()
            intercambio.sendResponseHeaders(200, bytes.size.toLong())
            intercambio.responseBody.use { it.write(bytes) }
        }
        start()
    }
    val credenciales = CredencialesLabsMobile("prueba@hospital.test", "token-secreto", simular, "http://127.0.0.1:${servidor.address.port}")
    try {
        bloque(CanalSmsLabsMobile(credenciales, HttpClient.newHttpClient(), LoggerFactory.getLogger("pruebas")), recibidas)
    } finally {
        servidor.stop(0)
    }
}
