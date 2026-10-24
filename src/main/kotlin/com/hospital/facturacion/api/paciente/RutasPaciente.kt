package com.hospital.facturacion.api.paciente

import com.hospital.facturacion.api.plataforma.AUTENTICACION_PACIENTE
import com.hospital.facturacion.api.plataforma.PacienteAutenticado
import com.hospital.facturacion.api.plataforma.SinCache
import com.hospital.facturacion.api.plataforma.recibirBytesHasta
import com.hospital.facturacion.api.plataforma.responderError
import com.hospital.facturacion.api.prefactura.aDto
import com.hospital.facturacion.domain.cita.AgendaCitas
import com.hospital.facturacion.domain.cita.Cita
import com.hospital.facturacion.domain.copago.MotivoRechazoPago
import com.hospital.facturacion.domain.copago.TokenPago
import com.hospital.facturacion.domain.formulario.FormularioPrefactura
import com.hospital.facturacion.domain.soporte.MotivoRechazoPdf
import com.hospital.facturacion.domain.soporte.TipoSoporte
import com.hospital.facturacion.service.paciente.CargarSoporte
import com.hospital.facturacion.service.paciente.ConsultarPrefacturaPaciente
import com.hospital.facturacion.service.paciente.ImpedimentoPago
import com.hospital.facturacion.service.paciente.IniciarSesionPaciente
import com.hospital.facturacion.service.paciente.PagarCopago
import com.hospital.facturacion.service.paciente.PrepararPagoCopago
import com.hospital.facturacion.service.paciente.RadicarSolicitud
import com.hospital.facturacion.service.paciente.ResultadoCarga
import com.hospital.facturacion.service.paciente.ResultadoPago
import com.hospital.facturacion.service.paciente.ResultadoPreparacion
import com.hospital.facturacion.service.paciente.ResultadoRadicacion
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

class CasosPaciente(
    val agenda: AgendaCitas,
    val formulario: FormularioPrefactura,
    val iniciarSesion: IniciarSesionPaciente,
    val consultar: ConsultarPrefacturaPaciente,
    val cargarSoporte: CargarSoporte,
    val prepararPago: PrepararPagoCopago,
    val pagarCopago: PagarCopago,
    val radicar: RadicarSolicitud,
    val tamanoMaximoSoporteBytes: Int,
)

fun Route.rutasPaciente(casos: CasosPaciente) {
    route("/api/paciente") {
        install(SinCache)
        post("/sesion") {
            val acceso = call.receive<AccesoPacienteDto>()
            val token = casos.iniciarSesion.ejecutar(acceso.identificacion, acceso.codigoAcceso)
            if (token == null) call.responderError(HttpStatusCode.Unauthorized, "credenciales") else call.respond(TokenDto(token.valor))
        }
        authenticate(AUTENTICACION_PACIENTE) {
            get("/prefactura") { conCita(casos) { call.respond(casos.consultar.ejecutar(it).aDto(casos.formulario, casos.tamanoMaximoSoporteBytes)) } }
            put("/soportes/{tipo}") { conCita(casos) { cargarSoporte(call, casos, it) } }
            post("/copago/checkout") { conCita(casos) { call.responderPreparacion(casos.prepararPago.ejecutar(it)) } }
            post("/copago") { conCita(casos) { pagarCopago(call, casos, it) } }
            post("/solicitud") {
                conCita(casos) { call.responderRadicacion(casos.radicar.ejecutar(it, call.receive<DatosPacienteDto>().datos)) }
            }
        }
    }
}

// La cita de la sesión puede haber salido de la agenda: se trata como sesión vencida.
private suspend fun RoutingContext.conCita(casos: CasosPaciente, accion: suspend (Cita) -> Unit) {
    val cita = call.principal<PacienteAutenticado>()?.cita?.let(casos.agenda::buscar)
    if (cita == null) call.respond(HttpStatusCode.Unauthorized) else accion(cita)
}

private suspend fun cargarSoporte(call: ApplicationCall, casos: CasosPaciente, cita: Cita) {
    val tipo = TipoSoporte.entries.firstOrNull { it.name == call.parameters["tipo"] }
        ?: return call.respond(HttpStatusCode.NotFound)
    val bytes = call.recibirBytesHasta(casos.tamanoMaximoSoporteBytes)
        ?: return call.responderError(HttpStatusCode.UnprocessableEntity, MotivoRechazoPdf.EXCEDE_TAMANO.name)
    when (val resultado = casos.cargarSoporte.ejecutar(cita.id, tipo, bytes)) {
        ResultadoCarga.Cargado -> call.respond(HttpStatusCode.NoContent)
        ResultadoCarga.NoEditable -> call.responderError(HttpStatusCode.Conflict, "no-editable")
        is ResultadoCarga.Rechazado -> call.responderError(HttpStatusCode.UnprocessableEntity, resultado.motivo.name)
    }
}

private suspend fun pagarCopago(call: ApplicationCall, casos: CasosPaciente, cita: Cita) {
    val medio = TokenPago.de(call.receive<MedioPagoDto>().token)
        ?: return call.responderError(HttpStatusCode.UnprocessableEntity, "pago-rechazado", listOf(MotivoRechazoPago.MEDIO_INVALIDO.name))
    call.responderPago(casos.pagarCopago.ejecutar(cita, medio))
}

private suspend fun ApplicationCall.responderPreparacion(resultado: ResultadoPreparacion) = when (resultado) {
    is ResultadoPreparacion.Lista -> respond(resultado.intencion.aDto())
    is ImpedimentoPago -> responderImpedimento(resultado)
}

private suspend fun ApplicationCall.responderPago(resultado: ResultadoPago) = when (resultado) {
    is ResultadoPago.Pagado -> respond(resultado.pago.aDto())
    is ResultadoPago.Rechazado -> responderError(HttpStatusCode.UnprocessableEntity, "pago-rechazado", listOf(resultado.motivo.name))
    is ImpedimentoPago -> responderImpedimento(resultado)
}

private suspend fun ApplicationCall.responderImpedimento(impedimento: ImpedimentoPago) = when (impedimento) {
    ResultadoPago.SinCopago -> responderError(HttpStatusCode.Conflict, "sin-copago")
    ResultadoPago.YaPagado -> responderError(HttpStatusCode.Conflict, "ya-pagado")
    ResultadoPago.NoEditable -> responderError(HttpStatusCode.Conflict, "no-editable")
}

private suspend fun ApplicationCall.responderRadicacion(resultado: ResultadoRadicacion) = when (resultado) {
    is ResultadoRadicacion.Radicada -> respond(RadicacionDto(resultado.solicitud.consecutivo.valor, resultado.solicitud.estado))
    is ResultadoRadicacion.DatosInvalidos ->
        respond(HttpStatusCode.UnprocessableEntity, ErroresRadicacionDto(resultado.errores, emptySet()))
    is ResultadoRadicacion.FaltanSoportes ->
        respond(HttpStatusCode.UnprocessableEntity, ErroresRadicacionDto(emptyMap(), resultado.tipos))
    ResultadoRadicacion.NoEditable -> responderError(HttpStatusCode.Conflict, "no-editable")
}
