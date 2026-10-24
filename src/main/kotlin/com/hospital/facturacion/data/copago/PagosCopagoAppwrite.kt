package com.hospital.facturacion.data.copago

import com.hospital.facturacion.data.appwrite.TablaAppwrite
import com.hospital.facturacion.data.appwrite.fechaAppwrite
import com.hospital.facturacion.data.appwrite.idAppwrite
import com.hospital.facturacion.data.appwrite.instanteAppwrite
import com.hospital.facturacion.data.cita.BorradoPorCita
import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.cita.Copago
import com.hospital.facturacion.domain.copago.PagoCopago
import com.hospital.facturacion.domain.copago.PagosCopago
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FilaPago(val cita: String, val referencia: String, @SerialName("valor_pesos") val valorPesos: Long, val fecha: String)

/** Un pago por cita en `pagos_copago`; el último reemplaza al anterior. */
class PagosCopagoAppwrite(private val pagos: TablaAppwrite<FilaPago>) : PagosCopago, BorradoPorCita {
    override fun guardar(cita: CitaId, pago: PagoCopago) =
        pagos.guardar(idAppwrite(cita.valor), FilaPago(cita.valor, pago.referencia, pago.copago.valorPesos, pago.fecha.fechaAppwrite()))

    override fun buscar(cita: CitaId): PagoCopago? =
        pagos.leer(idAppwrite(cita.valor))?.let { PagoCopago(it.referencia, Copago(it.valorPesos), it.fecha.instanteAppwrite()) }

    override fun eliminar(cita: CitaId) = pagos.eliminar(idAppwrite(cita.valor))
}
