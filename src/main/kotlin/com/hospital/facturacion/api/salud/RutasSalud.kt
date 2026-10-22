package com.hospital.facturacion.api.salud

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.rutasSalud() {
    get("/salud") { call.respond(mapOf("estado" to "activo")) }
}
