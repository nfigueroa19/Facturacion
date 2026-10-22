package com.hospital.facturacion.api.plataforma

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.response.respond
import java.net.URI

fun Application.instalarPlataforma(origenesCors: List<String>) {
    install(ContentNegotiation) { json() }
    // Sin query string: ahí podrían viajar datos del paciente.
    install(CallLogging) { format { "${it.request.httpMethod.value} ${it.request.path()} ${it.response.status()?.value}" } }
    instalarCabecerasSeguridad()
    instalarCors(origenesCors)
    instalarErrores()
}

private fun Application.instalarCabecerasSeguridad() {
    install(DefaultHeaders) {
        header(HttpHeaders.Server, "prefactura")
        header("X-Content-Type-Options", "nosniff")
        header("X-Robots-Tag", "noindex, nofollow")
        header("Referrer-Policy", "no-referrer")
        header("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'")
    }
}

private fun Application.instalarCors(origenes: List<String>) {
    install(CORS) {
        allowMethod(HttpMethod.Get)
        origenes.map(URI::create).forEach { allowHost(it.authority, schemes = listOf(it.scheme)) }
    }
}

private fun Application.instalarErrores() {
    install(StatusPages) {
        exception<Throwable> { call, causa ->
            call.application.log.error("Error no controlado en {}", call.request.path(), causa)
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error interno"))
        }
    }
}
