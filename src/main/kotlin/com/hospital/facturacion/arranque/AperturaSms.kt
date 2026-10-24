package com.hospital.facturacion.arranque

import com.hospital.facturacion.data.comunicacion.CanalSmsLabsMobile
import com.hospital.facturacion.data.comunicacion.CanalSmsRestringido
import com.hospital.facturacion.data.comunicacion.CanalSmsSimulado
import com.hospital.facturacion.data.comunicacion.CanalSmsTwilio
import com.hospital.facturacion.data.comunicacion.CredencialesLabsMobile
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

/** Correo con el que se registró la cuenta de LabsMobile y token de API de su sección API Settings. */
const val ALIAS_LABSMOBILE_USUARIO = "labsmobile.usuario"
const val ALIAS_LABSMOBILE_TOKEN = "labsmobile.token"

/** `true`: LabsMobile valida cada envío sin entregarlo ni cobrarlo. Si falta, el envío es real. */
const val ALIAS_LABSMOBILE_SIMULAR = "labsmobile.simular"

/** Celulares de 10 dígitos separados por comas a los que LabsMobile puede escribir. */
const val ALIAS_LABSMOBILE_AUTORIZADOS = "labsmobile.autorizados"

private class ProveedorSms(val nombre: String, val canal: CanalSms, val autorizados: String?)

/**
 * LabsMobile si el almacén PKCS12 trae su usuario y su token; si no, Twilio si trae la cuenta, el token y el remitente;
 * si no, todos los SMS se simulan. Con cualquier proveedor, solo salen los SMS a celulares autorizados; el resto se simula.
 */
fun abrirCanalSms(secretos: FuenteSecretos?, http: HttpClient, registro: Logger): CanalSms {
    fun valor(alias: String) = secretos?.leer(alias)?.consumir { String(it).trim() }
    val proveedor = labsMobile(::valor, http, registro) ?: twilio(::valor, http, registro)
    if (proveedor == null) {
        registro.info("SMS simulados: el almacén no tiene credenciales de LabsMobile ni de Twilio")
        return CanalSmsSimulado()
    }
    val autorizados = proveedor.autorizados.orEmpty().split(',').map(String::trim).filter(String::isNotEmpty).toSet()
    registro.info("SMS por {} para {} celulares autorizados; el resto se simula", proveedor.nombre, autorizados.size)
    return CanalSmsRestringido(autorizados, proveedor.canal, CanalSmsSimulado())
}

private fun labsMobile(valor: (String) -> String?, http: HttpClient, registro: Logger): ProveedorSms? {
    val usuario = valor(ALIAS_LABSMOBILE_USUARIO) ?: return null
    val token = valor(ALIAS_LABSMOBILE_TOKEN) ?: return null
    val credenciales = CredencialesLabsMobile(usuario, token, simular = valor(ALIAS_LABSMOBILE_SIMULAR).toBoolean())
    return ProveedorSms("LabsMobile", CanalSmsLabsMobile(credenciales, http, registro), valor(ALIAS_LABSMOBILE_AUTORIZADOS))
}

private fun twilio(valor: (String) -> String?, http: HttpClient, registro: Logger): ProveedorSms? {
    val cuenta = valor(ALIAS_TWILIO_CUENTA) ?: return null
    val token = valor(ALIAS_TWILIO_TOKEN) ?: return null
    val remitente = valor(ALIAS_TWILIO_REMITENTE) ?: return null
    val canal = CanalSmsTwilio(CredencialesTwilio(cuenta, token, remitente), http, registro)
    return ProveedorSms("Twilio", canal, valor(ALIAS_TWILIO_AUTORIZADOS))
}
