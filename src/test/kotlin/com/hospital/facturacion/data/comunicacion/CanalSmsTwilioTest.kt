package com.hospital.facturacion.data.comunicacion

import com.hospital.facturacion.domain.comunicacion.CanalSms
import com.hospital.facturacion.domain.comunicacion.Celular
import com.hospital.facturacion.domain.comunicacion.EstadoEnvio
import com.hospital.facturacion.domain.comunicacion.MensajeSms
import com.hospital.facturacion.domain.comunicacion.ResultadoEnvio
import com.sun.net.httpserver.HttpServer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.net.http.HttpClient
import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertEquals

class CanalSmsTwilioTest {
    private val mensaje = MensajeSms(requireNotNull(Celular.de("3000000009")), "Su ID de acceso es ABCD2345")
    private val registro = LoggerFactory.getLogger(CanalSmsTwilioTest::class.java)

    @Test
    fun `envia con autenticacion basica, indicativo de Colombia y el texto del mensaje`() = conTwilio(201, registro) { canal, recibidas ->
        assertEquals(ResultadoEnvio(EstadoEnvio.ENVIADO, "twilio"), canal.enviar(mensaje))
        val (autorizacion, cuerpo) = recibidas.single()
        assertEquals("Basic " + Base64.getEncoder().encodeToString("AC123:token-secreto".toByteArray()), autorizacion)
        val campos = cuerpo.split('&').associate { it.substringBefore('=') to URLDecoder.decode(it.substringAfter('='), Charsets.UTF_8) }
        assertEquals(mapOf("To" to "+573000000009", "From" to "+15005550006", "Body" to "Su ID de acceso es ABCD2345"), campos)
    }

    @Test
    fun `un rechazo de Twilio queda como envio fallido`() = conTwilio(400, registro) { canal, _ ->
        assertEquals(ResultadoEnvio(EstadoEnvio.FALLIDO, "twilio"), canal.enviar(mensaje))
    }

    @Test
    fun `solo los celulares autorizados salen por el canal real`() {
        val real = CanalSms { ResultadoEnvio(EstadoEnvio.ENVIADO, "twilio") }
        val canal = CanalSmsRestringido(setOf("3000000009"), real, CanalSmsSimulado())
        assertEquals(EstadoEnvio.ENVIADO, canal.enviar(mensaje).estado)
        assertEquals(EstadoEnvio.SIMULADO, canal.enviar(mensaje.copy(destino = requireNotNull(Celular.de("3111111111")))).estado)
    }
}

/** Imitación local de la API de Twilio que responde `codigo` y guarda la autorización y el cuerpo de cada petición. */
private fun conTwilio(codigo: Int, registro: Logger, bloque: (CanalSmsTwilio, MutableList<Pair<String, String>>) -> Unit) {
    val recibidas = mutableListOf<Pair<String, String>>()
    val servidor = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
        createContext("/2010-04-01/Accounts/AC123/Messages.json") { intercambio ->
            recibidas += intercambio.requestHeaders.getFirst("Authorization") to intercambio.requestBody.readAllBytes().decodeToString()
            intercambio.sendResponseHeaders(codigo, -1)
            intercambio.close()
        }
        start()
    }
    val credenciales = CredencialesTwilio("AC123", "token-secreto", "+15005550006", "http://127.0.0.1:${servidor.address.port}")
    try {
        bloque(CanalSmsTwilio(credenciales, HttpClient.newHttpClient(), registro), recibidas)
    } finally {
        servidor.stop(0)
    }
}
