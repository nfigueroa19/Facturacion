package com.hospital.facturacion.arranque

import com.hospital.facturacion.api.facturador.CasosFacturador
import com.hospital.facturacion.api.paciente.CasosPaciente
import com.hospital.facturacion.data.archivos.AlmacenSoportesArchivo
import com.hospital.facturacion.data.archivos.DirectorioSeguro
import com.hospital.facturacion.data.cita.AgendaCitasMemoria
import com.hospital.facturacion.data.cita.leerCitasPrueba
import com.hospital.facturacion.data.copago.PagosCopagoMemoria
import com.hospital.facturacion.data.copago.PasarelaPagosSimulada
import com.hospital.facturacion.data.facturador.ClavePbkdf2
import com.hospital.facturacion.data.facturador.DirectorioFacturadoresMemoria
import com.hospital.facturacion.data.facturador.leerFacturadoresPrueba
import com.hospital.facturacion.data.formulario.leerFormulario
import com.hospital.facturacion.data.prefactura.GeneradorConsecutivoMemoria
import com.hospital.facturacion.data.prefactura.SolicitudPrefacturaRepositorioMemoria
import com.hospital.facturacion.data.sesion.SesionesMemoria
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
import java.nio.file.Files
import java.security.SecureRandom
import java.time.Clock

class ComposicionPrefactura(val configuracion: ConfiguracionSolicitudes, val reloj: Clock, val aleatorio: SecureRandom) {
    val agenda = AgendaCitasMemoria(leerCitasPrueba(configuracion.citasPrueba, Json, reloj))
    val formulario = leerFormulario(configuracion.formulario, Json)
    val solicitudes = SolicitudPrefacturaRepositorioMemoria()
    val soportes = AlmacenSoportesArchivo(
        DirectorioSeguro(Files.createDirectories(configuracion.directorioSoportes)),
        configuracion.tamanoMaximoSoporteBytes,
    )
    val pagos = PagosCopagoMemoria()
    val sesionesPaciente = SesionesMemoria<CitaId>(reloj, configuracion.duracionSesion, aleatorio)
    val sesionesFacturador = SesionesMemoria<Facturador>(reloj, configuracion.duracionSesion, aleatorio)
}

fun ComposicionPrefactura.casosPaciente() = CasosPaciente(
    agenda = agenda,
    formulario = formulario,
    iniciarSesion = IniciarSesionPaciente(agenda, sesionesPaciente),
    consultar = ConsultarPrefacturaPaciente(solicitudes, soportes, pagos),
    cargarSoporte = CargarSoporte(solicitudes, soportes, configuracion.tamanoMaximoSoporteBytes),
    pagarCopago = PagarCopago(solicitudes, pagos, PasarelaPagosSimulada(aleatorio), reloj),
    radicar = RadicarSolicitud(formulario, solicitudes, soportes, GeneradorConsecutivoMemoria(), reloj),
    tamanoMaximoSoporteBytes = configuracion.tamanoMaximoSoporteBytes,
)

fun ComposicionPrefactura.casosFacturador() = CasosFacturador(
    formulario = formulario,
    iniciarSesion = IniciarSesionFacturador(
        DirectorioFacturadoresMemoria(leerFacturadoresPrueba(configuracion.facturadoresPrueba, Json), ClavePbkdf2()),
        sesionesFacturador,
    ),
    listar = ListarSolicitudes(solicitudes, agenda, soportes, pagos),
    consultar = ConsultarSolicitud(solicitudes, agenda, soportes, pagos),
    obtenerSoporte = ObtenerSoporte(solicitudes, soportes),
    aprobar = AprobarSolicitud(solicitudes, agenda, reloj),
    registrarNovedad = RegistrarNovedad(solicitudes, reloj),
    confirmarLlegada = ConfirmarLlegada(solicitudes),
)
