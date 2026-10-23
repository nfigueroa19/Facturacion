package com.hospital.facturacion.arranque

import com.hospital.facturacion.api.facturador.CasosFacturador
import com.hospital.facturacion.api.paciente.CasosPaciente
import com.hospital.facturacion.data.cita.AgendaCitasMemoria
import com.hospital.facturacion.data.cita.leerCitasPrueba
import com.hospital.facturacion.data.copago.PagosCopagoPostgres
import com.hospital.facturacion.data.copago.PasarelaPagosSimulada
import com.hospital.facturacion.data.facturador.ClavePbkdf2
import com.hospital.facturacion.data.facturador.DirectorioFacturadoresPostgres
import com.hospital.facturacion.data.facturador.leerFacturadoresPrueba
import com.hospital.facturacion.data.facturador.sembrarFacturadores
import com.hospital.facturacion.data.formulario.leerFormulario
import com.hospital.facturacion.data.prefactura.GeneradorConsecutivoPostgres
import com.hospital.facturacion.data.prefactura.SolicitudPrefacturaRepositorioPostgres
import com.hospital.facturacion.data.sesion.SesionesMemoria
import com.hospital.facturacion.data.soporte.AlmacenSoportesPostgres
import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.facturador.Facturador
import com.hospital.facturacion.service.facturador.AprobarSolicitud
import com.hospital.facturacion.service.facturador.ConfirmarLlegada
import com.hospital.facturacion.service.facturador.ConsultarSolicitud
import com.hospital.facturacion.service.facturador.IniciarSesionFacturador
import com.hospital.facturacion.service.facturador.ListarSolicitudes
import com.hospital.facturacion.service.facturador.ObtenerSoporte
import com.hospital.facturacion.service.facturador.RegistrarNovedad
import com.hospital.facturacion.service.paciente.CargarSoporte
import com.hospital.facturacion.service.paciente.ConsultarPrefacturaPaciente
import com.hospital.facturacion.service.paciente.IniciarSesionPaciente
import com.hospital.facturacion.service.paciente.PagarCopago
import com.hospital.facturacion.service.paciente.RadicarSolicitud
import kotlinx.serialization.json.Json
import java.security.SecureRandom
import java.time.Clock
import javax.sql.DataSource

class ComposicionPrefactura(
    val configuracion: ConfiguracionSolicitudes,
    val baseDatos: DataSource,
    val reloj: Clock,
    val aleatorio: SecureRandom,
) {
    val agenda = AgendaCitasMemoria(leerCitasPrueba(configuracion.citasPrueba, Json, reloj))
    val formulario = leerFormulario(configuracion.formulario, Json)
    val solicitudes = SolicitudPrefacturaRepositorioPostgres(baseDatos, Json)
    val soportes = AlmacenSoportesPostgres(baseDatos, configuracion.tamanoMaximoSoporteBytes)
    val pagos = PagosCopagoPostgres(baseDatos)

    // Las sesiones siguen en memoria: tras un reinicio el paciente y el facturador vuelven a ingresar.
    val sesionesPaciente = SesionesMemoria<CitaId>(reloj, configuracion.duracionSesion, aleatorio)
    val sesionesFacturador = SesionesMemoria<Facturador>(reloj, configuracion.duracionSesion, aleatorio)

    init {
        sembrarFacturadores(baseDatos, leerFacturadoresPrueba(configuracion.facturadoresPrueba, Json))
    }
}

fun ComposicionPrefactura.casosPaciente() = CasosPaciente(
    agenda = agenda,
    formulario = formulario,
    iniciarSesion = IniciarSesionPaciente(agenda, sesionesPaciente),
    consultar = ConsultarPrefacturaPaciente(solicitudes, soportes, pagos),
    cargarSoporte = CargarSoporte(solicitudes, soportes, configuracion.tamanoMaximoSoporteBytes),
    pagarCopago = PagarCopago(solicitudes, pagos, PasarelaPagosSimulada(aleatorio), reloj),
    radicar = RadicarSolicitud(formulario, solicitudes, soportes, GeneradorConsecutivoPostgres(baseDatos), reloj),
    tamanoMaximoSoporteBytes = configuracion.tamanoMaximoSoporteBytes,
)

fun ComposicionPrefactura.casosFacturador() = CasosFacturador(
    formulario = formulario,
    iniciarSesion = IniciarSesionFacturador(DirectorioFacturadoresPostgres(baseDatos, ClavePbkdf2()), sesionesFacturador),
    listar = ListarSolicitudes(solicitudes, agenda, soportes, pagos),
    consultar = ConsultarSolicitud(solicitudes, agenda, soportes, pagos),
    obtenerSoporte = ObtenerSoporte(solicitudes, soportes),
    aprobar = AprobarSolicitud(solicitudes, agenda, reloj),
    registrarNovedad = RegistrarNovedad(solicitudes, reloj),
    confirmarLlegada = ConfirmarLlegada(solicitudes),
)
