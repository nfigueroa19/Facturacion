package com.hospital.facturacion.data.comunicacion

import com.hospital.facturacion.domain.comunicacion.CanalSms
import com.hospital.facturacion.domain.comunicacion.EstadoEnvio
import com.hospital.facturacion.domain.comunicacion.MensajeSms
import com.hospital.facturacion.domain.comunicacion.ResultadoEnvio
import org.slf4j.Logger
import java.io.IOException
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.time.Duration
import java.util.Base64
import kotlin.text.Charsets.UTF_8

/**
 * API REST de Twilio (`POST /Accounts/{sid}/Messages.json`). Credenciales del almacén PKCS12. El registro solo guarda
 * el código HTTP: ni el celular ni el texto, que lleva el ID de acceso del paciente.
 */
class CanalSmsTwilio(private val credenciales: CredencialesTwilio, private val http: HttpClient, private val registro: Logger) : CanalSms {
    override fun enviar(mensaje: MensajeSms): ResultadoEnvio {
        val formulario = mapOf("To" to "+57" + mensaje.destino.valor, "From" to credenciales.remitente, "Body" to mensaje.texto)
            .entries.joinToString("&") { (clave, valor) -> clave + "=" + URLEncoder.encode(valor, UTF_8) }
        val peticion = HttpRequest.newBuilder(URI.create("${credenciales.base}/2010-04-01/Accounts/${credenciales.cuenta}/Messages.json"))
            .timeout(Duration.ofSeconds(15))
            .header("Authorization", "Basic " + Base64.getEncoder().encodeToString("${credenciales.cuenta}:${credenciales.token}".toByteArray()))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(formulario))
            .build()
        val estado = try {
            http.send(peticion, BodyHandlers.discarding()).statusCode()
        } catch (e: IOException) {
            registro.warn("Twilio no respondió: {}", e.javaClass.simpleName)
            return ResultadoEnvio(EstadoEnvio.FALLIDO, CANAL)
        }
        if (estado != 201) registro.warn("Twilio respondió {}", estado)
        return ResultadoEnvio(if (estado == 201) EstadoEnvio.ENVIADO else EstadoEnvio.FALLIDO, CANAL)
    }

    private companion object {
        const val CANAL = "twilio"
    }
}

/** `base` cambia solo en pruebas; el token no aparece en `toString`. */
class CredencialesTwilio(val cuenta: String, val token: String, val remitente: String, val base: String = "https://api.twilio.com") {
    override fun toString() = "CredencialesTwilio($cuenta, ****, $remitente)"
}
