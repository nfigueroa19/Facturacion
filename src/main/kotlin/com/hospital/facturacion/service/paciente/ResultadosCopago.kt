package com.hospital.facturacion.service.paciente

import com.hospital.facturacion.domain.cita.Cita
import com.hospital.facturacion.domain.copago.IntencionPago
import com.hospital.facturacion.domain.copago.MotivoRechazoPago
import com.hospital.facturacion.domain.copago.PagoCopago
import com.hospital.facturacion.domain.copago.PagosCopago
import com.hospital.facturacion.domain.prefactura.SolicitudPrefacturaRepositorio
import com.hospital.facturacion.domain.prefactura.admiteCambiosDelPaciente

sealed interface ResultadoPago {
    data class Pagado(val pago: PagoCopago) : ResultadoPago

    data object SinCopago : ImpedimentoPago

    data object YaPagado : ImpedimentoPago

    data object NoEditable : ImpedimentoPago

    data class Rechazado(val motivo: MotivoRechazoPago) : ResultadoPago
}

sealed interface ResultadoPreparacion {
    data class Lista(val intencion: IntencionPago) : ResultadoPreparacion
}

/** Lo que impide cobrar el copago en la web. Aplica igual al preparar el checkout y al registrar el pago. */
sealed interface ImpedimentoPago : ResultadoPago, ResultadoPreparacion

/** La solicitud ya no admite cambios del paciente o el copago ya está pagado. */
internal fun impedimentoDeCobro(cita: Cita, solicitudes: SolicitudPrefacturaRepositorio, pagos: PagosCopago): ImpedimentoPago? = when {
    !solicitudes.buscarPorCita(cita.id).admiteCambiosDelPaciente() -> ResultadoPago.NoEditable
    pagos.buscar(cita.id) != null -> ResultadoPago.YaPagado
    else -> null
}
