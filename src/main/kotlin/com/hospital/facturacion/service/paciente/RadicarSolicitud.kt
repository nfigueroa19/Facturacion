package com.hospital.facturacion.service.paciente

import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.formulario.ErrorCampo
import com.hospital.facturacion.domain.formulario.FormularioPrefactura
import com.hospital.facturacion.domain.formulario.ResultadoDatos
import com.hospital.facturacion.domain.prefactura.CambioEstado
import com.hospital.facturacion.domain.prefactura.EstadoPrefactura
import com.hospital.facturacion.domain.prefactura.GeneradorConsecutivo
import com.hospital.facturacion.domain.prefactura.SolicitudPrefactura
import com.hospital.facturacion.domain.prefactura.SolicitudPrefacturaRepositorio
import com.hospital.facturacion.domain.prefactura.admiteCambiosDelPaciente
import com.hospital.facturacion.domain.soporte.AlmacenSoportes
import com.hospital.facturacion.domain.soporte.TipoSoporte
import java.time.Clock

sealed interface ResultadoRadicacion {
    data class Radicada(val solicitud: SolicitudPrefactura) : ResultadoRadicacion

    data class DatosInvalidos(val errores: Map<String, ErrorCampo>) : ResultadoRadicacion

    data class FaltanSoportes(val tipos: Set<TipoSoporte>) : ResultadoRadicacion

    data object NoEditable : ResultadoRadicacion
}

/** Primer envío o reenvío tras corregir novedades. En ambos casos la solicitud queda EN_REVISION. */
class RadicarSolicitud(
    private val formulario: FormularioPrefactura,
    private val solicitudes: SolicitudPrefacturaRepositorio,
    private val soportes: AlmacenSoportes,
    private val consecutivos: GeneradorConsecutivo,
    private val reloj: Clock,
) {
    fun ejecutar(cita: CitaId, valores: Map<String, String>): ResultadoRadicacion {
        val actual = solicitudes.buscarPorCita(cita)
        if (!actual.admiteCambiosDelPaciente()) return ResultadoRadicacion.NoEditable
        val datos = when (val resultado = formulario.validar(valores)) {
            is ResultadoDatos.Invalidos -> return ResultadoRadicacion.DatosInvalidos(resultado.errores)
            is ResultadoDatos.Validos -> resultado.datos
        }
        val faltantes = TipoSoporte.entries.toSet() - soportes.cargados(cita)
        if (faltantes.isNotEmpty()) return ResultadoRadicacion.FaltanSoportes(faltantes)
        val radicada = when (val cambio = actual?.corregir(datos, reloj.instant())) {
            null -> SolicitudPrefactura(consecutivos.siguiente(), cita, EstadoPrefactura.EN_REVISION, reloj.instant(), datos)
            is CambioEstado.Aplicado -> cambio.solicitud
            is CambioEstado.Rechazado -> return ResultadoRadicacion.NoEditable
        }
        solicitudes.guardar(radicada)
        return ResultadoRadicacion.Radicada(radicada)
    }
}
