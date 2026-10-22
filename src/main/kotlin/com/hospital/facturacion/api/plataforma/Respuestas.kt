package com.hospital.facturacion.api.plataforma

import io.ktor.http.CacheControl
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.request.header
import io.ktor.server.request.receiveChannel
import io.ktor.server.response.cacheControl
import io.ktor.server.response.respond
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray
import kotlinx.serialization.Serializable

/** Código estable que el frontend traduce con `config/contenido.json`. */
@Serializable
data class ErrorDto(val error: String, val detalle: List<String> = emptyList())

suspend fun ApplicationCall.responderError(estado: HttpStatusCode, codigo: String, detalle: List<String> = emptyList()) =
    respond(estado, ErrorDto(codigo, detalle))

/** Las respuestas con datos del paciente no se guardan en cachés del navegador ni de intermediarios. */
val SinCache = createRouteScopedPlugin("SinCache") {
    onCall { it.response.cacheControl(CacheControl.NoStore(null)) }
}

/**
 * Devuelve null si el cuerpo declara más de `limite` bytes. Si no declara tamaño, lee como máximo `limite + 1`
 * bytes y la validación del dominio detecta el exceso, sin cargar en memoria un cuerpo grande.
 */
suspend fun ApplicationCall.recibirBytesHasta(limite: Int): ByteArray? {
    val declarado = request.header(HttpHeaders.ContentLength)?.toLongOrNull()
    if (declarado != null && declarado > limite) return null
    return receiveChannel().readRemaining(limite + 1L).readByteArray()
}
