package com.hospital.facturacion.data.copago

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.slf4j.Logger
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers
import java.time.Duration

/** GET a la API de Wompi. Null si Wompi no respondió; el registro guarda solo el tipo de falla. */
internal fun HttpClient.consultarWompi(uri: String, registro: Logger, vararg cabeceras: Pair<String, String>): HttpResponse<String>? {
    val peticion = HttpRequest.newBuilder(URI.create(uri)).timeout(Duration.ofSeconds(15))
    cabeceras.forEach { (nombre, valor) -> peticion.header(nombre, valor) }
    return try {
        send(peticion.GET().build(), BodyHandlers.ofString())
    } catch (e: IOException) {
        registro.warn("Wompi no respondió: {}", e.javaClass.simpleName)
        null
    }
}

/** Lee el objeto `data` de una respuesta de Wompi. Null si falta un campo o el cuerpo no es el esperado. */
internal fun <T> datosWompi(cuerpo: String, lectura: (JsonObject) -> T): T? = try {
    lectura(Json.parseToJsonElement(cuerpo).jsonObject.getValue("data").jsonObject)
} catch (e: IllegalArgumentException) {
    null
} catch (e: NoSuchElementException) {
    null
}
