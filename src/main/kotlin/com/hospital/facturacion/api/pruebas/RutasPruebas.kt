package com.hospital.facturacion.api.pruebas

import com.hospital.facturacion.api.plataforma.AUTENTICACION_FACTURADOR
import com.hospital.facturacion.api.plataforma.SinCache
import io.ktor.http.ContentType
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable

/** `catalogoJson` es el catálogo de pruebas ya validado; `restablecer` devuelve cuántos escenarios sembró. */
class CasosPruebas(val catalogoJson: String, val restablecer: () -> Int)

@Serializable
data class EscenariosRestablecidosDto(val escenarios: Int)

/**
 * Solo se instalan con el catálogo de pruebas configurado. El catálogo es público a propósito: lo lee la pantalla de
 * acceso para mostrar las credenciales ficticias. Restablecer borra datos y exige la sesión del personal.
 */
fun Route.rutasPruebas(casos: CasosPruebas) {
    route("/api/dev") {
        install(SinCache)
        get("/catalogo") { call.respondText(casos.catalogoJson, ContentType.Application.Json) }
        authenticate(AUTENTICACION_FACTURADOR) {
            post("/poblar") { call.respond(EscenariosRestablecidosDto(casos.restablecer())) }
        }
    }
}
