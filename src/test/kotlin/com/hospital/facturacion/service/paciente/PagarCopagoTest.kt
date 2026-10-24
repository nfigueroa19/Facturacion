package com.hospital.facturacion.service.paciente

import com.hospital.facturacion.citaConCopago
import com.hospital.facturacion.citaSinCopago
import com.hospital.facturacion.data.copago.PagosCopagoMemoria
import com.hospital.facturacion.domain.copago.MotivoRechazoPago
import com.hospital.facturacion.domain.copago.PagoCopago
import com.hospital.facturacion.domain.copago.PasarelaPagos
import com.hospital.facturacion.domain.copago.ResultadoPasarela
import com.hospital.facturacion.domain.copago.TokenPago
import com.hospital.facturacion.domain.prefactura.EstadoPrefactura
import com.hospital.facturacion.relojDePrueba
import com.hospital.facturacion.repositorioCon
import com.hospital.facturacion.solicitudDePrueba
import kotlin.test.Test
import kotlin.test.assertEquals

class PagarCopagoTest {
    private val pagos = PagosCopagoMemoria()
    private var cobros = 0
    private val medio = requireNotNull(TokenPago.de("tok_prueba"))
    private val aprueba = PasarelaPagos { _, _, _ ->
        cobros++
        ResultadoPasarela.Aprobado("REF-1")
    }

    private fun pagar(pasarela: PasarelaPagos = aprueba, estado: EstadoPrefactura? = null) =
        PagarCopago(repositorioCon(*listOfNotNull(estado?.let(::solicitudDePrueba)).toTypedArray()), pagos, pasarela, relojDePrueba)

    @Test
    fun `registra el pago aprobado y no cobra dos veces`() {
        val pago = PagoCopago("REF-1", requireNotNull(citaConCopago.copago), relojDePrueba.instant())
        assertEquals(ResultadoPago.Pagado(pago), pagar().ejecutar(citaConCopago, medio))
        assertEquals(pago, pagos.buscar(citaConCopago.id))
        assertEquals(ResultadoPago.YaPagado, pagar().ejecutar(citaConCopago, medio))
        assertEquals(1, cobros)
    }

    @Test
    fun `no cobra sin copago activo, con la solicitud en revision ni registra pagos rechazados`() {
        assertEquals(ResultadoPago.SinCopago, pagar().ejecutar(citaSinCopago, medio))
        assertEquals(ResultadoPago.NoEditable, pagar(estado = EstadoPrefactura.EN_REVISION).ejecutar(citaConCopago, medio))
        val rechaza = PasarelaPagos { _, _, _ -> ResultadoPasarela.Rechazado(MotivoRechazoPago.FONDOS_INSUFICIENTES) }
        assertEquals(ResultadoPago.Rechazado(MotivoRechazoPago.FONDOS_INSUFICIENTES), pagar(rechaza).ejecutar(citaConCopago, medio))
        assertEquals(null, pagos.buscar(citaConCopago.id))
        assertEquals(0, cobros)
    }
}
