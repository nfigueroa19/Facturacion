package com.hospital.facturacion.data.pruebas

import com.hospital.facturacion.domain.prefactura.CambioEstado
import com.hospital.facturacion.domain.prefactura.EstadoPrefactura
import com.hospital.facturacion.domain.prefactura.Novedad
import com.hospital.facturacion.domain.prefactura.NumeroDinamica
import com.hospital.facturacion.domain.prefactura.Observacion
import com.hospital.facturacion.domain.prefactura.Registro
import com.hospital.facturacion.domain.prefactura.SolicitudPrefactura
import com.hospital.facturacion.domain.prefactura.Validacion
import com.hospital.facturacion.domain.prefactura.anularIngreso
import com.hospital.facturacion.domain.prefactura.registrarFactura
import com.hospital.facturacion.domain.prefactura.registrarIngreso
import java.time.Instant

/**
 * Hasta dónde avanzó la solicitud de un escenario. Cada etapa aplica sobre la anterior el mismo paso del dominio que
 * haría el facturador, así que un escenario nunca queda en un estado al que el diagrama no permite llegar.
 */
enum class EtapaEscenario(private val anterior: EtapaEscenario?, private val paso: SolicitudPrefactura.(Instant) -> CambioEstado) {
    SIN_RADICAR(null, { CambioEstado.Aplicado(this) }),
    EN_REVISION(null, { CambioEstado.Aplicado(this) }),
    PENDIENTE(EN_REVISION, { registrarNovedad(requireNotNull(Novedad.de(NOVEDAD, FACTURADOR, it))) }),
    APROBADO(EN_REVISION, { aprobar(Validacion(FACTURADOR, it)) }),
    APROBADO_CON_INGRESO(APROBADO, { registrarIngreso(registro("ING-PRUEBA-001", it)) }),
    LISTO_PARA_FACTURAR(APROBADO_CON_INGRESO, { cambiarEstado(EstadoPrefactura.LISTO_PARA_FACTURAR) }),
    FACTURADO(LISTO_PARA_FACTURAR, { registrarFactura(registro("FAC-PRUEBA-001", it)) }),
    INGRESO_ANULADO(APROBADO_CON_INGRESO, { anularIngreso(Registro(requireNotNull(Observacion.de(MOTIVO_ANULACION)), FACTURADOR, it)) }),
    ;

    val radicada: Boolean get() = this != SIN_RADICAR

    /** Lleva una solicitud recién radicada (EN_REVISION) hasta esta etapa. */
    fun aplicar(recienRadicada: SolicitudPrefactura, fecha: Instant): SolicitudPrefactura {
        val previa = anterior?.aplicar(recienRadicada, fecha) ?: recienRadicada
        val cambio = previa.paso(fecha)
        check(cambio is CambioEstado.Aplicado) { "La etapa $name no se puede aplicar: $cambio" }
        return cambio.solicitud
    }
}

private const val FACTURADOR = "facturador"
private const val NOVEDAD = "La orden médica de prueba no se puede leer. Cárguela de nuevo en PDF."
private const val MOTIVO_ANULACION = "El paciente de prueba no llegó a la cita."

private fun registro(numero: String, fecha: Instant) = Registro(requireNotNull(NumeroDinamica.de(numero)), FACTURADOR, fecha)
