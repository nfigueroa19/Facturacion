package com.hospital.facturacion.data.copago

import com.hospital.facturacion.domain.copago.AceptacionPasarela
import com.hospital.facturacion.domain.copago.ContratoPasarela
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.slf4j.Logger
import java.net.http.HttpClient

/** Reglamento de usuarios y autorización de datos personales del comercio, con tokens de un solo uso. */
fun interface ContratosWompi {
    fun consultar(): AceptacionPasarela?
}

/**
 * `GET /v1/merchants/info` con la llave pública en `x-merchant-public-key`. Reemplaza a `GET /v1/merchants/{llave}`, que
 * Wompi retira el 2026-10-31. Lo consulta el backend porque el CORS de Wompi no admite esa cabecera desde el navegador.
 */
class ClienteContratosWompi(private val credenciales: CredencialesWompi, private val http: HttpClient, private val registro: Logger) :
    ContratosWompi {
    override fun consultar(): AceptacionPasarela? {
        val respuesta = http.consultarWompi("${credenciales.base}/v1/merchants/info", registro, "x-merchant-public-key" to credenciales.publica)
            ?: return null
        if (respuesta.statusCode() != 200) {
            registro.warn("Wompi respondió {} al consultar el comercio", respuesta.statusCode())
            return null
        }
        return datosWompi(respuesta.body()) { AceptacionPasarela(it.contrato("presigned_acceptance"), it.contrato("presigned_personal_data_auth")) }
    }
}

private fun JsonObject.contrato(campo: String): ContratoPasarela {
    val contrato = getValue(campo).jsonObject
    return ContratoPasarela(contrato.getValue("acceptance_token").jsonPrimitive.content, contrato.getValue("permalink").jsonPrimitive.content)
}
