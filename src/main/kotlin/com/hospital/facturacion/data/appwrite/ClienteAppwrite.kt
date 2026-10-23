package com.hospital.facturacion.data.appwrite

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/**
 * API REST de servidor de un proyecto de Appwrite. Un código inesperado detiene la operación con el código HTTP,
 * sin la ruta (lleva datos de consulta), el cuerpo ni la clave. La clave no aparece en `toString`.
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
        return http.send(peticion, cuerpo).also {
            check(it.statusCode() in esperados) { "Appwrite respondió ${it.statusCode()}; se esperaba ${esperados.joinToString()}" }
        }
    }

    override fun toString() = "ClienteAppwrite($base, $proyecto, ****)"
}
