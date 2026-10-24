package com.hospital.facturacion.service.paciente

import com.hospital.facturacion.citaConCopago
import com.hospital.facturacion.citaSinCopago
import com.hospital.facturacion.data.copago.PagosCopagoMemoria
import com.hospital.facturacion.domain.copago.AceptacionPasarela
import com.hospital.facturacion.domain.copago.CheckoutPasarela
import com.hospital.facturacion.domain.copago.ContratoPasarela
import com.hospital.facturacion.domain.copago.IntencionPago
import com.hospital.facturacion.domain.copago.PagoCopago
import com.hospital.facturacion.domain.prefactura.EstadoPrefactura
import com.hospital.facturacion.relojDePrueba
import com.hospital.facturacion.repositorioCon
import com.hospital.facturacion.solicitudDePrueba
import kotlin.test.Test
import kotlin.test.assertEquals

class PrepararPagoCopagoTest {
    private val pagos = PagosCopagoMemoria()
    private val contrato = ContratoPasarela("tok", "https://wompi.test/contrato.pdf")
    private val intencion = IntencionPago("pub_test_prueba", "cita-1_ref", 500_000, "COP", null, AceptacionPasarela(contrato, contrato))
    private val checkout = CheckoutPasarela { _, copago -> intencion.copy(montoCentavos = copago.valorPesos * 100) }

    private fun preparar(estado: EstadoPrefactura? = null, pasarela: CheckoutPasarela = checkout) =
        PrepararPagoCopago(repositorioCon(*listOfNotNull(estado?.let(::solicitudDePrueba)).toTypedArray()), pagos, pasarela)

    @Test
    fun `prepara el checkout con el valor del copago mientras la solicitud admite cambios`() {
        assertEquals(ResultadoPreparacion.Lista(intencion), preparar().ejecutar(citaConCopago))
        assertEquals(ResultadoPreparacion.Lista(intencion), preparar(EstadoPrefactura.PENDIENTE).ejecutar(citaConCopago))
    }

    @Test
    fun `si la pasarela no entrega los contratos de aceptacion, la pasarela no esta disponible`() {
        assertEquals(ResultadoPreparacion.PasarelaNoDisponible, preparar(pasarela = { _, _ -> null }).ejecutar(citaConCopago))
    }

    @Test
    fun `no prepara cobros sin copago, con la solicitud en revision ni con el copago ya pagado`() {
        assertEquals(ResultadoPago.SinCopago, preparar().ejecutar(citaSinCopago))
        assertEquals(ResultadoPago.NoEditable, preparar(EstadoPrefactura.EN_REVISION).ejecutar(citaConCopago))
        pagos.guardar(citaConCopago.id, PagoCopago("REF-1", requireNotNull(citaConCopago.copago), relojDePrueba.instant()))
        assertEquals(ResultadoPago.YaPagado, preparar().ejecutar(citaConCopago))
    }
}
