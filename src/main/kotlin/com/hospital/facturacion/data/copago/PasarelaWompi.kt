package com.hospital.facturacion.data.copago

import com.hospital.facturacion.domain.cita.Cita
import com.hospital.facturacion.domain.cita.Copago
import com.hospital.facturacion.domain.copago.MotivoRechazoPago
import com.hospital.facturacion.domain.copago.PasarelaPagos
import com.hospital.facturacion.domain.copago.ResultadoPasarela
import com.hospital.facturacion.domain.copago.TokenPago

/**
 * Verifica en Wompi la transacción que el widget creó en el navegador; `medio` es su ID. Solo la da por pagada si es de
 * esta cita, por el valor del copago y en pesos. Si no, es un medio inválido aunque Wompi la haya aprobado.
 */
class PasarelaWompi(private val transacciones: TransaccionesWompi) : PasarelaPagos {
    override fun cobrar(cita: Cita, copago: Copago, medio: TokenPago): ResultadoPasarela =
        when (val consulta = transacciones.consultar(medio.valor)) {
            ConsultaTransaccion.NoExiste -> ResultadoPasarela.Rechazado(MotivoRechazoPago.MEDIO_INVALIDO)
            ConsultaTransaccion.Falla -> ResultadoPasarela.Rechazado(MotivoRechazoPago.ERROR_PASARELA)
            is ConsultaTransaccion.Encontrada -> resultado(consulta.transaccion, cita, copago)
        }
}

// Una transacción PENDING no se da por pagada: el paciente reintenta o paga en ventanilla (RN-10).
private fun resultado(transaccion: TransaccionWompi, cita: Cita, copago: Copago): ResultadoPasarela {
    val corresponde = transaccion.referencia.startsWith(prefijoReferencia(cita.id)) &&
        transaccion.montoCentavos == copago.centavos() &&
        transaccion.moneda == MONEDA_WOMPI
    if (!corresponde) return ResultadoPasarela.Rechazado(MotivoRechazoPago.MEDIO_INVALIDO)
    return when (transaccion.estado) {
        EstadoTransaccionWompi.APPROVED -> ResultadoPasarela.Aprobado(transaccion.id)
        EstadoTransaccionWompi.DECLINED, EstadoTransaccionWompi.VOIDED -> ResultadoPasarela.Rechazado(MotivoRechazoPago.RECHAZADA)
        EstadoTransaccionWompi.ERROR, EstadoTransaccionWompi.PENDING -> ResultadoPasarela.Rechazado(MotivoRechazoPago.ERROR_PASARELA)
    }
}
