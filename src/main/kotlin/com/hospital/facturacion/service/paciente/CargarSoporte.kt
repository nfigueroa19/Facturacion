package com.hospital.facturacion.service.paciente

import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.prefactura.SolicitudPrefacturaRepositorio
import com.hospital.facturacion.domain.prefactura.admiteCambiosDelPaciente
import com.hospital.facturacion.domain.soporte.AlmacenSoportes
import com.hospital.facturacion.domain.soporte.DocumentoPdf
import com.hospital.facturacion.domain.soporte.MotivoRechazoPdf
import com.hospital.facturacion.domain.soporte.TipoSoporte
import com.hospital.facturacion.domain.soporte.ValidacionPdf

sealed interface ResultadoCarga {
    data object Cargado : ResultadoCarga

    data object NoEditable : ResultadoCarga

    data class Rechazado(val motivo: MotivoRechazoPdf) : ResultadoCarga
}

/** El tamaño máximo es un límite técnico de configuración, no una regla de negocio (P-08). */
class CargarSoporte(
    private val solicitudes: SolicitudPrefacturaRepositorio,
    private val almacen: AlmacenSoportes,
    private val tamanoMaximoBytes: Int,
) {
    fun ejecutar(cita: CitaId, tipo: TipoSoporte, bytes: ByteArray): ResultadoCarga {
        if (!solicitudes.buscarPorCita(cita).admiteCambiosDelPaciente()) return ResultadoCarga.NoEditable
        return when (val validacion = DocumentoPdf.validar(bytes, tamanoMaximoBytes)) {
            is ValidacionPdf.Invalido -> ResultadoCarga.Rechazado(validacion.motivo)
            is ValidacionPdf.Valido -> ResultadoCarga.Cargado.also { almacen.guardar(cita, tipo, validacion.pdf) }
        }
    }
}
