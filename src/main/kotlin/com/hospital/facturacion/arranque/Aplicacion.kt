package com.hospital.facturacion.arranque

import com.hospital.facturacion.api.contenido.rutasContenido
import com.hospital.facturacion.api.facturador.rutasFacturador
import com.hospital.facturacion.api.paciente.rutasPaciente
import com.hospital.facturacion.api.personal.rutasPersonal
import com.hospital.facturacion.api.plataforma.instalarAutenticacion
import com.hospital.facturacion.api.plataforma.instalarPlataforma
import com.hospital.facturacion.api.salud.rutasSalud
import com.hospital.facturacion.data.comunicacion.PlantillasSmsContenido
import com.hospital.facturacion.service.comunicacion.NotificarPorSms
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.log
import io.ktor.server.netty.EngineMain
import io.ktor.server.routing.routing
import java.net.http.HttpClient
import java.security.SecureRandom
import java.time.Clock
import java.time.Duration

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.modulo() = moduloCon(System::getenv)

/** Raíz de composición: el único lugar donde se instancian implementaciones concretas. */
fun Application.moduloCon(entorno: (String) -> String?) {
    val configuracion = environment.config.configuracionAplicacion()
    val secretos = abrirSecretos(configuracion.secretos, entorno, log)
    val reloj = Clock.system(configuracion.solicitudes.zonaHoraria)
    val persistencia = abrirPersistencia(configuracion, secretos, reloj)
    monitor.subscribe(ApplicationStopped) { persistencia.cerrar() }
    log.info("Persistencia en {}", configuracion.persistencia.destino)
    val contenido = ComposicionContenido(configuracion.contenido, log)
    val http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build()
    monitor.subscribe(ApplicationStopped) { http.close() }
    val canalSms = abrirCanalSms(secretos, http, log)
    val notificador = NotificarPorSms(canalSms, persistencia.comunicaciones, PlantillasSmsContenido(contenido.fuente), reloj)
    val prefactura = ComposicionPrefactura(configuracion.solicitudes, persistencia, notificador, reloj, SecureRandom())
    instalarPlataforma(configuracion.origenesCors)
    instalarAutenticacion(prefactura.sesionesPaciente, prefactura.sesionesFacturador)
    routing {
        rutasSalud()
        rutasContenido(contenido.textos, contenido.imagenes)
        rutasPaciente(prefactura.casosPaciente())
        rutasFacturador(prefactura.casosFacturador())
        rutasPersonal(prefactura.casosPersonal())
    }
}
