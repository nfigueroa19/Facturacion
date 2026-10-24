package com.hospital.facturacion.service.paciente

import com.hospital.facturacion.domain.cita.Cita
import com.hospital.facturacion.domain.copago.MotivoRechazoPago
import com.hospital.facturacion.domain.copago.PagoCopago
import com.hospital.facturacion.domain.copago.PagosCopago
import com.hospital.facturacion.domain.copago.PasarelaPagos
import com.hospital.facturacion.domain.copago.ResultadoPasarela
import com.hospital.facturacion.domain.copago.TokenPago
import com.hospital.facturacion.domain.prefactura.SolicitudPrefacturaRepositorio
import com.hospital.facturacion.domain.prefactura.admiteCambiosDelPaciente
import java.time.Clock

sealed interface ResultadoPago {
    data class Pagado(val pago: PagoCopago) : ResultadoPago

    data object SinCopago : ResultadoPago

    data object YaPagado : ResultadoPago

    data object NoEditable : ResultadoPago

    data class Rechazado(val motivo: MotivoRechazoPago) : ResultadoPago
}

/** El pago en la web es opcional (RN-10): si no paga o la pasarela lo rechaza, puede reintentar o pagar en ventanilla. */
class PagarCopago(
    private val solicitudes: SolicitudPrefacturaRepositorio,
    private val pagos: PagosCopago,
    private val pasarela: PasarelaPagos,
    private val reloj: Clock,
) {
    fun ejecutar(cita: Cita, medio: TokenPago): ResultadoPago {
        val copago = cita.copago ?: return ResultadoPago.SinCopago
        if (!solicitudes.buscarPorCita(cita.id).admiteCambiosDelPaciente()) return ResultadoPago.NoEditable
        if (pagos.buscar(cita.id) != null) return ResultadoPago.YaPagado
        return when (val cobro = pasarela.cobrar(cita, copago, medio)) {
            is ResultadoPasarela.Rechazado -> ResultadoPago.Rechazado(cobro.motivo)
            is ResultadoPasarela.Aprobado -> PagoCopago(cobro.referencia, copago, reloj.instant())
                .also { pagos.guardar(cita.id, it) }
                .let(ResultadoPago::Pagado)
        }
    }
}
