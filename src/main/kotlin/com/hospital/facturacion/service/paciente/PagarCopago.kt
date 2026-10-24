package com.hospital.facturacion.service.paciente

import com.hospital.facturacion.domain.cita.Cita
import com.hospital.facturacion.domain.copago.PagoCopago
import com.hospital.facturacion.domain.copago.PagosCopago
import com.hospital.facturacion.domain.copago.PasarelaPagos
import com.hospital.facturacion.domain.copago.ResultadoPasarela
import com.hospital.facturacion.domain.copago.TokenPago
import com.hospital.facturacion.domain.prefactura.SolicitudPrefacturaRepositorio
import java.time.Clock

/** El pago en la web es opcional (RN-10): si no paga o la pasarela lo rechaza, puede reintentar o pagar en ventanilla. */
class PagarCopago(
    private val solicitudes: SolicitudPrefacturaRepositorio,
    private val pagos: PagosCopago,
    private val pasarela: PasarelaPagos,
    private val reloj: Clock,
) {
    fun ejecutar(cita: Cita, medio: TokenPago): ResultadoPago {
        val copago = cita.copago ?: return ResultadoPago.SinCopago
        impedimentoDeCobro(cita, solicitudes, pagos)?.let { return it }
        return when (val cobro = pasarela.cobrar(cita, copago, medio)) {
            is ResultadoPasarela.Rechazado -> ResultadoPago.Rechazado(cobro.motivo)
            is ResultadoPasarela.Aprobado -> PagoCopago(cobro.referencia, copago, reloj.instant())
                .also { pagos.guardar(cita.id, it) }
                .let(ResultadoPago::Pagado)
        }
    }
}
