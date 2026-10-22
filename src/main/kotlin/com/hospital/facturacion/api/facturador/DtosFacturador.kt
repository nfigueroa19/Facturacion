package com.hospital.facturacion.api.facturador

import com.hospital.facturacion.api.prefactura.CitaDto
import com.hospital.facturacion.api.prefactura.PagoDto
import com.hospital.facturacion.api.prefactura.aDto
import com.hospital.facturacion.domain.formulario.FormularioPrefactura
import com.hospital.facturacion.domain.prefactura.EstadoPrefactura
import com.hospital.facturacion.domain.prefactura.ItemRevision
import com.hospital.facturacion.domain.soporte.TipoSoporte
import com.hospital.facturacion.service.facturador.SolicitudRevisable
import kotlinx.serialization.Serializable

@Serializable
data class AccesoFacturadorDto(val usuario: String, val clave: String)

@Serializable
data class SesionFacturadorDto(val token: String, val nombre: String)

@Serializable
data class ResumenSolicitudDto(
    val consecutivo: String,
    val estado: EstadoPrefactura,
    val fechaEnvio: String,
    val identificacion: String,
    val cita: CitaDto,
    val soportes: Set<TipoSoporte>,
    val pago: PagoDto?,
)

@Serializable
data class DatoDto(val clave: String, val etiqueta: String, val valor: String)

@Serializable
data class NovedadFacturadorDto(val descripcion: String, val facturador: String, val fecha: String)

@Serializable
data class ValidacionDto(val facturador: String, val fecha: String)

@Serializable
data class DetalleSolicitudDto(
    val resumen: ResumenSolicitudDto,
    val datos: List<DatoDto>,
    val novedades: List<NovedadFacturadorDto>,
    val validacion: ValidacionDto?,
    val checklist: Set<ItemRevision>,
)

@Serializable
data class AprobacionDto(val revisados: Set<ItemRevision>)

@Serializable
data class NovedadNuevaDto(val descripcion: String)

fun SolicitudRevisable.aResumen() = ResumenSolicitudDto(
    consecutivo = solicitud.consecutivo.valor,
    estado = solicitud.estado,
    fechaEnvio = solicitud.fechaEnvio.toString(),
    identificacion = expediente.cita.identificacionPaciente,
    cita = expediente.cita.aDto(),
    soportes = expediente.soportes,
    pago = expediente.pago?.aDto(),
)

fun SolicitudRevisable.aDetalle(formulario: FormularioPrefactura) = DetalleSolicitudDto(
    resumen = aResumen(),
    datos = datosConEtiqueta(formulario),
    novedades = solicitud.novedades.map { NovedadFacturadorDto(it.descripcion, it.facturador, it.fecha.toString()) },
    validacion = solicitud.validacion?.let { ValidacionDto(it.facturador, it.fecha.toString()) },
    checklist = ItemRevision.requeridosPara(expediente.cita),
)

// Un valor cuyo campo ya no está en el formulario (se editó la configuración) se muestra con su clave.
private fun SolicitudRevisable.datosConEtiqueta(formulario: FormularioPrefactura): List<DatoDto> {
    val etiquetas = formulario.campos.associate { it.clave to it.etiqueta }
    val orden = formulario.campos.map { it.clave }
    return solicitud.datos.valores.entries
        .sortedBy { orden.indexOf(it.key).takeIf { i -> i >= 0 } ?: Int.MAX_VALUE }
        .map { DatoDto(it.key, etiquetas[it.key] ?: it.key, it.value) }
}
