package com.hospital.facturacion.data.appwrite

import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/**
 * API REST de servidor de un proyecto de Appwrite. Un código inesperado detiene la operación con el código HTTP,
 * sin la ruta (lleva datos de consulta), el cuerpo ni la clave. La clave no aparece en `toString`.
 * Sin respuesta, 429 o 5xx lanzan [FalloTransitorioAppwrite] para que quien llama decida si reintenta.
 */
class ClienteAppwrite(endpoint: URI, private val proyecto: String, private val claveApi: String, private val http: HttpClient) {
    private val base = endpoint.toString().trimEnd('/')

    fun <T> enviar(
        ruta: String,
        cuerpo: HttpResponse.BodyHandler<T>,
        vararg esperados: Int,
        metodo: HttpRequest.Builder.() -> HttpRequest.Builder,
    ): HttpResponse<T> {
        val peticion = HttpRequest.newBuilder(URI.create(base + ruta))
            .timeout(Duration.ofSeconds(30))
            .header("X-Appwrite-Project", proyecto)
            .header("X-Appwrite-Key", claveApi)
            .metodo()
            .build()
        val respuesta = try {
            http.send(peticion, cuerpo)
        } catch (e: IOException) {
            throw FalloTransitorioAppwrite("Appwrite no respondió (${e.javaClass.simpleName})", e)
        }
        val estado = respuesta.statusCode()
        if (estado !in esperados && (estado == 429 || estado >= 500)) throw FalloTransitorioAppwrite("Appwrite respondió $estado")
        check(estado in esperados) { "Appwrite respondió $estado; se esperaba ${esperados.joinToString()}" }
        return respuesta
    }

    override fun toString() = "ClienteAppwrite($base, $proyecto, ****)"
}
