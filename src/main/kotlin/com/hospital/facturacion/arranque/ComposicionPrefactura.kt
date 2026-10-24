package com.hospital.facturacion.arranque

import com.hospital.facturacion.api.facturador.CasosFacturador
import com.hospital.facturacion.api.paciente.CasosPaciente
import com.hospital.facturacion.api.personal.CasosPersonal
import com.hospital.facturacion.data.cita.GeneradorAccesoAleatorio
import com.hospital.facturacion.data.formulario.leerFormulario
import com.hospital.facturacion.data.sesion.SesionesMemoria
import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.comunicacion.NotificadorPaciente
import com.hospital.facturacion.domain.facturador.Facturador
import com.hospital.facturacion.service.callcenter.ProgramarCita
import com.hospital.facturacion.service.comunicacion.ConsultarComunicaciones
import com.hospital.facturacion.service.digiturno.AtenderLlegada
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
import com.hospital.facturacion.service.paciente.CargarSoporte
import com.hospital.facturacion.service.paciente.ConsultarPrefacturaPaciente
import com.hospital.facturacion.service.paciente.IniciarSesionPaciente
import com.hospital.facturacion.service.paciente.PagarCopago
import com.hospital.facturacion.service.paciente.PrepararPagoCopago
import com.hospital.facturacion.service.paciente.RadicarSolicitud
import kotlinx.serialization.json.Json
import java.security.SecureRandom
import java.time.Clock

class ComposicionPrefactura(
    val configuracion: ConfiguracionSolicitudes,
    val persistencia: Persistencia,
    val notificador: NotificadorPaciente,
    val pasarela: PasarelaCopago,
    val reloj: Clock,
    val aleatorio: SecureRandom,
) {
    val formulario = leerFormulario(configuracion.formulario, Json)

    // Las sesiones siguen en memoria: tras un reinicio el paciente y el personal vuelven a ingresar.
    val sesionesPaciente = SesionesMemoria<CitaId>(reloj, configuracion.duracionSesion, aleatorio)
    val sesionesFacturador = SesionesMemoria<Facturador>(reloj, configuracion.duracionSesion, aleatorio)
}

fun ComposicionPrefactura.casosPaciente() = with(persistencia) {
    CasosPaciente(
        agenda = agenda,
        formulario = formulario,
        iniciarSesion = IniciarSesionPaciente(agenda, sesionesPaciente),
        consultar = ConsultarPrefacturaPaciente(solicitudes, soportes, pagos),
        cargarSoporte = CargarSoporte(solicitudes, soportes, configuracion.tamanoMaximoSoporteBytes),
        prepararPago = PrepararPagoCopago(solicitudes, pagos, pasarela.checkout),
        pagarCopago = PagarCopago(solicitudes, pagos, pasarela.cobro, reloj),
        radicar = RadicarSolicitud(formulario, solicitudes, soportes, consecutivo, notificador, reloj),
        tamanoMaximoSoporteBytes = configuracion.tamanoMaximoSoporteBytes,
    )
}

fun ComposicionPrefactura.casosFacturador() = with(persistencia) {
    CasosFacturador(
        formulario = formulario,
        iniciarSesion = IniciarSesionFacturador(facturadores, sesionesFacturador),
        listar = ListarSolicitudes(solicitudes, agenda, soportes, pagos),
        consultar = ConsultarSolicitud(solicitudes, agenda, soportes, pagos),
        obtenerSoporte = ObtenerSoporte(solicitudes, soportes),
        aprobar = AprobarSolicitud(solicitudes, agenda, reloj),
        registrarNovedad = RegistrarNovedad(solicitudes, agenda, notificador, reloj),
        confirmarLlegada = ConfirmarLlegada(solicitudes),
        registrarIngreso = RegistrarIngreso(solicitudes, agenda, reloj),
        anularIngreso = AnularIngreso(solicitudes, reloj),
        registrarIntento = RegistrarIntentoFacturacion(solicitudes, agenda, reloj),
        registrarFactura = RegistrarFactura(solicitudes, agenda, reloj),
    )
}

fun ComposicionPrefactura.casosPersonal() = with(persistencia) {
    CasosPersonal(
        programarCita = ProgramarCita(registroCitas, GeneradorAccesoAleatorio(aleatorio), notificador, reloj),
        atenderLlegada = AtenderLlegada(agenda, solicitudes, turnos, reloj),
        comunicaciones = ConsultarComunicaciones(comunicaciones),
    )
}
