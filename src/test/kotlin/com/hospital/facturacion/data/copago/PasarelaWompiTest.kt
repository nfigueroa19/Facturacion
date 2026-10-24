package com.hospital.facturacion.data.copago

import com.hospital.facturacion.citaConCopago
import com.hospital.facturacion.domain.cita.Copago
import com.hospital.facturacion.domain.copago.MotivoRechazoPago
import com.hospital.facturacion.domain.copago.ResultadoPasarela
import com.hospital.facturacion.domain.copago.TokenPago
import kotlin.test.Test
import kotlin.test.assertEquals

class PasarelaWompiTest {
    private val copago = Copago(5000)
    private val medio = requireNotNull(TokenPago.de("15113-1729800000-12345"))

    private fun transaccion(estado: EstadoTransaccionWompi, referencia: String = "cita-1_0a0a0a0a0a0a", monto: Long = 500_000, moneda: String = "COP") =
        ConsultaTransaccion.Encontrada(TransaccionWompi(medio.valor, estado, referencia, monto, moneda))

    private fun cobrar(consulta: ConsultaTransaccion) = PasarelaWompi { consulta }.cobrar(citaConCopago, copago, medio)

    @Test
    fun `aprueba con el ID de la transaccion y rechaza segun el estado de Wompi`() {
        assertEquals(ResultadoPasarela.Aprobado(medio.valor), cobrar(transaccion(EstadoTransaccionWompi.APPROVED)))
        assertEquals(ResultadoPasarela.Rechazado(MotivoRechazoPago.RECHAZADA), cobrar(transaccion(EstadoTransaccionWompi.DECLINED)))
        assertEquals(ResultadoPasarela.Rechazado(MotivoRechazoPago.RECHAZADA), cobrar(transaccion(EstadoTransaccionWompi.VOIDED)))
        assertEquals(ResultadoPasarela.Rechazado(MotivoRechazoPago.ERROR_PASARELA), cobrar(transaccion(EstadoTransaccionWompi.ERROR)))
        assertEquals(ResultadoPasarela.Rechazado(MotivoRechazoPago.ERROR_PASARELA), cobrar(transaccion(EstadoTransaccionWompi.PENDING)))
        assertEquals(ResultadoPasarela.Rechazado(MotivoRechazoPago.ERROR_PASARELA), cobrar(ConsultaTransaccion.Falla))
        assertEquals(ResultadoPasarela.Rechazado(MotivoRechazoPago.MEDIO_INVALIDO), cobrar(ConsultaTransaccion.NoExiste))
    }

    @Test
    fun `una transaccion aprobada de otra cita, por otro valor o en otra moneda es un medio invalido`() {
        val invalido = ResultadoPasarela.Rechazado(MotivoRechazoPago.MEDIO_INVALIDO)
        assertEquals(invalido, cobrar(transaccion(EstadoTransaccionWompi.APPROVED, referencia = "cita-1-a_0a0a0a0a0a0a")))
        assertEquals(invalido, cobrar(transaccion(EstadoTransaccionWompi.APPROVED, monto = 100)))
        assertEquals(invalido, cobrar(transaccion(EstadoTransaccionWompi.APPROVED, moneda = "USD")))
    }
}
