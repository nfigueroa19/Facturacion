package com.hospital.facturacion.arranque

import com.hospital.facturacion.api.contenido.rutasContenido
import com.hospital.facturacion.api.facturador.rutasFacturador
import com.hospital.facturacion.api.paciente.rutasPaciente
import com.hospital.facturacion.api.plataforma.instalarAutenticacion
import com.hospital.facturacion.api.plataforma.instalarPlataforma
import com.hospital.facturacion.api.salud.rutasSalud
import io.ktor.server.application.Application
import io.ktor.server.application.log
import io.ktor.server.netty.EngineMain
import io.ktor.server.routing.routing
import java.security.SecureRandom
import java.time.Clock

fun main(args: Array<String>) = EngineMain.main(args)

/** Raíz de composición: el único lugar donde se instancian implementaciones concretas. */
fun Application.modulo() {
    val configuracion = environment.config.configuracionAplicacion()
    abrirSecretos(configuracion.secretos, System::getenv, log)
    val contenido = ComposicionContenido(configuracion.contenido, log)
    val reloj = Clock.system(configuracion.solicitudes.zonaHoraria)
    val prefactura = ComposicionPrefactura(configuracion.solicitudes, reloj, SecureRandom())
    instalarPlataforma(configuracion.origenesCors)
    instalarAutenticacion(prefactura.sesionesPaciente, prefactura.sesionesFacturador)
    routing {
        rutasSalud()
        rutasContenido(contenido.textos, contenido.imagenes)
        rutasPaciente(prefactura.casosPaciente())
        rutasFacturador(prefactura.casosFacturador())
    }
}
