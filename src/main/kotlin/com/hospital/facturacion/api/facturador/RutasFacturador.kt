package com.hospital.facturacion.api.facturador

import com.hospital.facturacion.api.plataforma.AUTENTICACION_FACTURADOR
import com.hospital.facturacion.api.plataforma.SinCache
import com.hospital.facturacion.api.plataforma.responderError
import com.hospital.facturacion.domain.facturador.Facturador
import com.hospital.facturacion.domain.formulario.FormularioPrefactura
import com.hospital.facturacion.domain.prefactura.ConsecutivoRadicado
import com.hospital.facturacion.domain.prefactura.EstadoPrefactura
import com.hospital.facturacion.domain.soporte.TipoSoporte
import com.hospital.facturacion.service.facturador.AnularIngreso
import com.hospital.facturacion.service.facturador.AprobarSolicitud
import com.hospital.facturacion.service.facturador.ConfirmarLlegada
import com.hospital.facturacion.service.facturador.ConsultarSolicitud
import com.hospital.facturacion.service.facturador.IniciarSesionFacturador
import com.hospital.facturacion.service.facturador.ListarSolicitudes
import com.hospital.facturacion.service.facturador.ObtenerSoporte
import com.hospital.facturacion.service.facturador.RegistrarFactura
import com.hospital.facturacion.service.facturador.RegistrarIngreso
import com.hospital.facturacion.service.facturador.RegistrarIntentoFacturacion
import com.hospital.facturacion.service.facturador.RegistrarNovedad
import com.hospital.facturacion.service.facturador.ResultadoAccion
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.util.getOrFail

class CasosFacturador(
    val formulario: FormularioPrefactura,
    val iniciarSesion: IniciarSesionFacturador,
    val listar: ListarSolicitudes,
    val consultar: ConsultarSolicitud,
    val obtenerSoporte: ObtenerSoporte,
    val aprobar: AprobarSolicitud,
    val registrarNovedad: RegistrarNovedad,
    val confirmarLlegada: ConfirmarLlegada,
    val registrarIngreso: RegistrarIngreso,
    val anularIngreso: AnularIngreso,
    val registrarIntento: RegistrarIntentoFacturacion,
    val registrarFactura: RegistrarFactura,
)

fun Route.rutasFacturador(casos: CasosFacturador) {
    route("/api/facturador") {
        install(SinCache)
        post("/sesion") {
            val acceso = call.receive<AccesoFacturadorDto>()
            val sesion = casos.iniciarSesion.ejecutar(acceso.usuario, acceso.clave)
            if (sesion == null) call.responderError(HttpStatusCode.Unauthorized, "credenciales")
            else call.respond(SesionFacturadorDto(sesion.token.valor, sesion.facturador.nombre))
        }
        authenticate(AUTENTICACION_FACTURADOR) { rutasSolicitudes(casos) }
    }
}

private fun Route.rutasSolicitudes(casos: CasosFacturador) = route("/solicitudes") {
    get {
        val filtro = call.request.queryParameters["estado"]
        val estado = filtro?.let { EstadoPrefactura.entries.firstOrNull { e -> e.name == it } }
        if (filtro != null && estado == null) return@get call.responderError(HttpStatusCode.BadRequest, "estado")
        call.respond(casos.listar.ejecutar(estado).map { it.aResumen() })
    }
    route("/{consecutivo}") {
        get {
            val revisable = casos.consultar.ejecutar(consecutivo()) ?: return@get call.respond(HttpStatusCode.NotFound)
            call.respond(revisable.aDetalle(casos.formulario))
        }
        get("/soportes/{tipo}") {
            val tipo = TipoSoporte.entries.firstOrNull { it.name == call.parameters["tipo"] }
            val pdf = tipo?.let { casos.obtenerSoporte.ejecutar(consecutivo(), it) } ?: return@get call.respond(HttpStatusCode.NotFound)
            call.respondBytes(pdf.contenido, ContentType.Application.Pdf)
        }
        post("/aprobar") {
            call.responderAccion(casos.aprobar.ejecutar(consecutivo(), call.receive<AprobacionDto>().revisados, facturador()))
        }
        post("/novedades") {
            call.responderAccion(casos.registrarNovedad.ejecutar(consecutivo(), call.receive<NovedadNuevaDto>().descripcion, facturador()))
        }
        post("/llegada") { call.responderAccion(casos.confirmarLlegada.ejecutar(consecutivo())) }
        rutasAtencion(casos)
    }
}

// Pasos del diagrama del facturador después de la aprobación: ingreso, anulación, intentos de facturación y factura.
private fun Route.rutasAtencion(casos: CasosFacturador) {
    post("/ingreso") {
        call.responderAccion(casos.registrarIngreso.ejecutar(consecutivo(), call.receive<NumeroDinamicaDto>().numero, facturador()))
    }
    post("/anulacion") {
        call.responderAccion(casos.anularIngreso.ejecutar(consecutivo(), call.receive<ObservacionDto>().texto, facturador()))
    }
    post("/intentos-facturacion") {
        call.responderAccion(casos.registrarIntento.ejecutar(consecutivo(), call.receive<ObservacionDto>().texto, facturador()))
    }
    post("/factura") {
        call.responderAccion(casos.registrarFactura.ejecutar(consecutivo(), call.receive<NumeroDinamicaDto>().numero, facturador()))
    }
}

private fun RoutingContext.consecutivo() = ConsecutivoRadicado(call.parameters.getOrFail("consecutivo"))

// authenticate(AUTENTICACION_FACTURADOR) garantiza el principal; si faltara, es un error de configuración de rutas.
private fun RoutingContext.facturador(): Facturador = checkNotNull(call.principal<Facturador>()) { "Ruta del facturador sin autenticar" }

private suspend fun ApplicationCall.responderAccion(resultado: ResultadoAccion) = when (resultado) {
    is ResultadoAccion.Aplicada -> respond(HttpStatusCode.NoContent)
    ResultadoAccion.NoEncontrada -> respond(HttpStatusCode.NotFound)
    is ResultadoAccion.TransicionInvalida ->
        responderError(HttpStatusCode.Conflict, "transicion", listOf(resultado.actual.name, resultado.destino.name))
    is ResultadoAccion.ChecklistIncompleto ->
        responderError(HttpStatusCode.UnprocessableEntity, "checklist", resultado.faltantes.map { it.name })
    ResultadoAccion.NovedadInvalida -> responderError(HttpStatusCode.UnprocessableEntity, "novedad")
    ResultadoAccion.DatoInvalido -> responderError(HttpStatusCode.UnprocessableEntity, "dato")
    is ResultadoAccion.ReglaIncumplida -> responderError(HttpStatusCode.Conflict, "atencion", listOf(resultado.motivo.name))
}
