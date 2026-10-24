package com.hospital.facturacion

import com.hospital.facturacion.data.prefactura.SolicitudPrefacturaRepositorioMemoria
import com.hospital.facturacion.domain.cita.AgendaCitas
import com.hospital.facturacion.domain.cita.Cita
import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.cita.Copago
import com.hospital.facturacion.domain.cita.Regimen
import com.hospital.facturacion.domain.comunicacion.Comunicacion
import com.hospital.facturacion.domain.comunicacion.MotivoComunicacion
import com.hospital.facturacion.domain.comunicacion.NotificadorPaciente
import com.hospital.facturacion.domain.formulario.CampoFormulario
import com.hospital.facturacion.domain.formulario.FormularioPrefactura
import com.hospital.facturacion.domain.formulario.TipoCampo
import com.hospital.facturacion.domain.prefactura.ConsecutivoRadicado
import com.hospital.facturacion.domain.prefactura.DatosFacturacion
import com.hospital.facturacion.domain.prefactura.EstadoPrefactura
import com.hospital.facturacion.domain.prefactura.SolicitudPrefactura
import com.hospital.facturacion.domain.soporte.AlmacenSoportes
import com.hospital.facturacion.domain.soporte.DocumentoPdf
import com.hospital.facturacion.domain.soporte.TipoSoporte
import com.hospital.facturacion.domain.soporte.ValidacionPdf
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

// Datos ficticios y dobles de los puertos de la prefactura.

val relojDePrueba: Clock = Clock.fixed(Instant.parse("2026-10-22T13:00:00Z"), ZoneOffset.UTC)
val citaIdDePrueba = requireNotNull(CitaId.de("cita-1"))
val citaConCopago = Cita(citaIdDePrueba, "1000000001", LocalDate.of(2026, 10, 23), "Médico", "Especialidad", Regimen.CONTRIBUTIVO, Copago(5000))
val citaSinCopago = citaConCopago.copy(regimen = Regimen.SUBSIDIADO, copago = null)
val datosDePrueba = DatosFacturacion(mapOf("nombre-padre" to "Padre de prueba"))
val formularioDePrueba = FormularioPrefactura(
    listOf(
        CampoFormulario("nombre-padre", "Nombre del padre", TipoCampo.TEXTO, obligatorio = true),
        CampoFormulario("correo", "Correo", TipoCampo.CORREO, obligatorio = false),
    ),
)

fun pdfDePrueba(texto: String = "%PDF-1.7 prueba"): DocumentoPdf =
    when (val validacion = DocumentoPdf.validar(texto.toByteArray(), 1024)) {
        is ValidacionPdf.Valido -> validacion.pdf
        is ValidacionPdf.Invalido -> error("PDF de prueba inválido: ${validacion.motivo}")
    }

fun solicitudDePrueba(estado: EstadoPrefactura) =
    SolicitudPrefactura(ConsecutivoRadicado("PF-1"), citaIdDePrueba, estado, Instant.EPOCH, datosDePrueba)

fun repositorioCon(vararg solicitudes: SolicitudPrefactura) =
    SolicitudPrefacturaRepositorioMemoria().apply { solicitudes.forEach(::guardar) }

/** Acepta el código `CODIGO` para cualquier cita registrada. */
class AgendaDePrueba(private vararg val citas: Cita) : AgendaCitas {
    override fun buscar(id: CitaId) = citas.firstOrNull { it.id == id }

    override fun buscarPorAcceso(identificacion: String, codigoAcceso: String) =
        citas.firstOrNull { it.identificacionPaciente == identificacion && codigoAcceso == "CODIGO" }

    override fun buscarPorIdentificacion(identificacion: String) = citas.filter { it.identificacionPaciente == identificacion }
}

/** Guarda cada aviso en lugar de enviarlo. */
class NotificadorDePrueba : NotificadorPaciente {
    val avisos = mutableListOf<Triple<Cita, MotivoComunicacion, Map<String, String>>>()

    override fun notificar(cita: Cita, motivo: MotivoComunicacion, valores: Map<String, String>): Comunicacion? {
        avisos += Triple(cita, motivo, valores)
        return null
    }
}

class SoportesEnMemoria(vararg cargados: TipoSoporte) : AlmacenSoportes {
    val archivos = cargados.associateWith { pdfDePrueba() }.toMutableMap()

    override fun guardar(cita: CitaId, tipo: TipoSoporte, pdf: DocumentoPdf) {
        archivos[tipo] = pdf
    }

    override fun leer(cita: CitaId, tipo: TipoSoporte) = archivos[tipo]

    override fun cargados(cita: CitaId) = archivos.keys.toSet()
}
