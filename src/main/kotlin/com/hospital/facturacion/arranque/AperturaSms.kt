package com.hospital.facturacion.arranque

import com.hospital.facturacion.data.comunicacion.CanalSmsRestringido
import com.hospital.facturacion.data.comunicacion.CanalSmsSimulado
import com.hospital.facturacion.data.comunicacion.CanalSmsTwilio
import com.hospital.facturacion.data.comunicacion.CredencialesTwilio
import com.hospital.facturacion.data.secretos.FuenteSecretos
import com.hospital.facturacion.domain.comunicacion.CanalSms
import org.slf4j.Logger
import java.net.http.HttpClient

const val ALIAS_TWILIO_CUENTA = "twilio.cuenta"
const val ALIAS_TWILIO_TOKEN = "twilio.token"
const val ALIAS_TWILIO_REMITENTE = "twilio.remitente"

/** Celulares de 10 dígitos separados por comas: los verificados en la cuenta de pruebas de Twilio. */
const val ALIAS_TWILIO_AUTORIZADOS = "twilio.autorizados"

/**
 * Twilio solo si el almacén PKCS12 trae la cuenta, el token y el remitente; si no, todos los SMS se simulan.
 * Aun con Twilio, solo salen los SMS a celulares autorizados; el resto se simula.
 */
fun abrirCanalSms(secretos: FuenteSecretos?, http: HttpClient, registro: Logger): CanalSms {
    fun valor(alias: String) = secretos?.leer(alias)?.consumir { String(it).trim() }
    val cuenta = valor(ALIAS_TWILIO_CUENTA)
    val token = valor(ALIAS_TWILIO_TOKEN)
    val remitente = valor(ALIAS_TWILIO_REMITENTE)
    if (cuenta == null || token == null || remitente == null) {
        registro.info("SMS simulados: el almacén no tiene credenciales de Twilio")
        return CanalSmsSimulado()
    }
    val autorizados = valor(ALIAS_TWILIO_AUTORIZADOS).orEmpty().split(',').map(String::trim).filter(String::isNotEmpty).toSet()
    registro.info("SMS por Twilio para {} celulares autorizados; el resto se simula", autorizados.size)
    return CanalSmsRestringido(autorizados, CanalSmsTwilio(CredencialesTwilio(cuenta, token, remitente), http, registro), CanalSmsSimulado())
}
