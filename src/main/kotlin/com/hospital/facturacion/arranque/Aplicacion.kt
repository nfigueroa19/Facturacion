package com.hospital.facturacion.arranque

import com.hospital.facturacion.api.contenido.rutasContenido
import com.hospital.facturacion.api.plataforma.instalarPlataforma
import com.hospital.facturacion.api.salud.rutasSalud
import io.ktor.server.application.Application
import io.ktor.server.application.log
import io.ktor.server.netty.EngineMain
import io.ktor.server.routing.routing

fun main(args: Array<String>) = EngineMain.main(args)

/** Raíz de composición: el único lugar donde se instancian implementaciones concretas. */
fun Application.modulo() {
    val configuracion = environment.config.configuracionAplicacion()
    abrirSecretos(configuracion.secretos, System::getenv, log)
    val contenido = ComposicionContenido(configuracion.contenido, log)
    instalarPlataforma(configuracion.origenesCors)
    routing {
        rutasSalud()
        rutasContenido(contenido.textos, contenido.imagenes)
    }
}
