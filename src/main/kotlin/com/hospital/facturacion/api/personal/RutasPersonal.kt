package com.hospital.facturacion.api.personal

import com.hospital.facturacion.api.plataforma.AUTENTICACION_FACTURADOR
import com.hospital.facturacion.api.plataforma.SinCache
import com.hospital.facturacion.service.callcenter.ProgramarCita
import com.hospital.facturacion.service.callcenter.ResultadoProgramacion
import com.hospital.facturacion.service.comunicacion.ConsultarComunicaciones
import com.hospital.facturacion.service.digiturno.AtenderLlegada
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

class CasosPersonal(
    val programarCita: ProgramarCita,
    val atenderLlegada: AtenderLlegada,
    val comunicaciones: ConsultarComunicaciones,
)

/**
 * Call center, digiturno y panel de comunicaciones. En el piloto los usa el personal con la misma sesión del panel
 * del facturador: crean citas, envían SMS y cambian estados, así que nunca quedan abiertos al público.
 */
fun Route.rutasPersonal(casos: CasosPersonal) {
    authenticate(AUTENTICACION_FACTURADOR) {
        route("/api/callcenter") {
            install(SinCache)
            post("/citas") {
                when (val resultado = casos.programarCita.ejecutar(call.receive<NuevaCitaDto>().aDominio())) {
                    is ResultadoProgramacion.Programada -> call.respond(HttpStatusCode.Created, resultado.aDto())
                    is ResultadoProgramacion.Invalida -> call.respond(HttpStatusCode.UnprocessableEntity, ErroresCitaDto(resultado.errores))
                }
            }
        }
        route("/api/digiturno") {
            install(SinCache)
            post("/llegadas") { call.respond(casos.atenderLlegada.ejecutar(call.receive<LecturaDigiturnoDto>().identificacion).aDto()) }
        }
        route("/api/personal") {
            install(SinCache)
            get("/comunicaciones") { call.respond(casos.comunicaciones.ejecutar().map { it.aDto() }) }
        }
    }
}
