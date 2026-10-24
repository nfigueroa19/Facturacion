package com.hospital.facturacion.data.comunicacion

import com.hospital.facturacion.domain.comunicacion.CanalSms
import com.hospital.facturacion.domain.comunicacion.EstadoEnvio
import com.hospital.facturacion.domain.comunicacion.MensajeSms
import com.hospital.facturacion.domain.comunicacion.ResultadoEnvio
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import org.slf4j.Logger
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.time.Duration
import java.util.Base64

/**
 * API REST JSON de LabsMobile (`POST /json/send`) con autenticación básica: correo de la cuenta y token de API.
 * Con `simular`, LabsMobile valida el envío sin entregarlo ni descontar saldo (`"test": 1`) y el SMS queda SIMULADO.
 * El registro solo guarda los códigos de respuesta: ni el celular ni el texto, que lleva el ID de acceso.
 */
class CanalSmsLabsMobile(private val credenciales: CredencialesLabsMobile, private val http: HttpClient, private val registro: Logger) : CanalSms {
    override fun enviar(mensaje: MensajeSms): ResultadoEnvio {
        val autorizacion = Base64.getEncoder().encodeToString("${credenciales.usuario}:${credenciales.token}".toByteArray())
        val peticion = HttpRequest.newBuilder(URI.create("${credenciales.base}/json/send"))
            .timeout(Duration.ofSeconds(15))
            .header("Authorization", "Basic $autorizacion")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(cuerpo(mensaje, credenciales.simular)))
            .build()
        val respuesta = try {
            http.send(peticion, BodyHandlers.ofString())
        } catch (e: IOException) {
            registro.warn("LabsMobile no respondió: {}", e.javaClass.simpleName)
            return ResultadoEnvio(EstadoEnvio.FALLIDO, CANAL)
        }
        val codigo = codigoDe(respuesta.body())
        if (respuesta.statusCode() != 200 || codigo != "0") {
            registro.warn("LabsMobile respondió {} con código {}", respuesta.statusCode(), codigo)
            return ResultadoEnvio(EstadoEnvio.FALLIDO, CANAL)
        }
        return ResultadoEnvio(if (credenciales.simular) EstadoEnvio.SIMULADO else EstadoEnvio.ENVIADO, CANAL)
    }

    private companion object {
        const val CANAL = "labsmobile"
    }
}

/** `base` cambia solo en pruebas; el token no aparece en `toString`. */
class CredencialesLabsMobile(val usuario: String, val token: String, val simular: Boolean, val base: String = "https://api.labsmobile.com") {
    override fun toString() = "CredencialesLabsMobile($usuario, ****, simular=$simular)"
}

// Los textos llevan tildes y pasan de 160 caracteres: `long` concatena y `ucs2` conserva lo que no cabe en GSM 7 bits.
private fun cuerpo(mensaje: MensajeSms, simular: Boolean): String = buildJsonObject {
    put("message", mensaje.texto)
    putJsonArray("recipient") { addJsonObject { put("msisdn", "57" + mensaje.destino.valor) } }
    put("long", 1)
    if (mensaje.texto.any { it !in GSM_BASICO }) put("ucs2", 1)
    if (simular) put("test", 1)
}.toString()

private fun codigoDe(cuerpo: String): String? = try {
    Json.parseToJsonElement(cuerpo).jsonObject["code"]?.jsonPrimitive?.content
} catch (e: IllegalArgumentException) {
    null
}

private const val GSM_BASICO = "@£\$¥èéùìòÇ\nØø\rÅåΔ_ΦΓΛΩΠΨΣΘΞÆæßÉ !\"#¤%&'()*+,-./0123456789:;<=>?¡ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÑÜ§¿abcdefghijklmnopqrstuvwxyzäöñüà"
