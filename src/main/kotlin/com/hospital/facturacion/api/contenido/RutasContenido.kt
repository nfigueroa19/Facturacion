package com.hospital.facturacion.api.contenido

import com.hospital.facturacion.domain.contenido.ClaveRecurso
import com.hospital.facturacion.service.contenido.ConsultarTextosInstitucionales
import com.hospital.facturacion.service.contenido.ObtenerImagenInstitucional
import com.hospital.facturacion.service.contenido.ResultadoImagen
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.log
import io.ktor.server.response.cacheControl
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.rutasContenido(textos: ConsultarTextosInstitucionales, imagenes: ObtenerImagenInstitucional) {
    route("/api/contenido") {
        get("/textos") {
            call.response.cacheControl(CacheControl.NoCache(null))
            call.respond(textos.ejecutar())
        }
        get("/imagenes/{clave}") {
            val clave = call.parameters["clave"]?.let(ClaveRecurso::de)
            if (clave == null) call.respond(HttpStatusCode.NotFound) else responderImagen(call, clave, imagenes.ejecutar(clave))
        }
    }
}

// Todo rechazo responde 404 para no revelar si el archivo existe; el motivo queda en el registro.
private suspend fun responderImagen(call: ApplicationCall, clave: ClaveRecurso, resultado: ResultadoImagen) {
    when (resultado) {
        is ResultadoImagen.Encontrada -> {
            call.response.cacheControl(CacheControl.NoCache(null))
            call.respondBytes(resultado.imagen.contenido, ContentType.parse(resultado.imagen.formato.tipoMime))
        }
        else -> {
            call.application.log.warn("Imagen '{}' no servida: {}", clave.valor, resultado)
            call.respond(HttpStatusCode.NotFound)
        }
    }
}
