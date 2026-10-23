package com.hospital.facturacion.data.copago

import com.hospital.facturacion.data.postgres.consultar
import com.hospital.facturacion.data.postgres.ejecutar
import com.hospital.facturacion.data.postgres.instante
import com.hospital.facturacion.data.postgres.utc
import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.cita.Copago
import com.hospital.facturacion.domain.copago.PagoCopago
import com.hospital.facturacion.domain.copago.PagosCopago
import javax.sql.DataSource

class PagosCopagoPostgres(private val fuente: DataSource) : PagosCopago {
    override fun guardar(cita: CitaId, pago: PagoCopago) {
        fuente.ejecutar(
            """
            INSERT INTO pago_copago (cita, referencia, valor_pesos, fecha) VALUES (?, ?, ?, ?)
            ON CONFLICT (cita) DO UPDATE SET referencia = EXCLUDED.referencia,
                valor_pesos = EXCLUDED.valor_pesos, fecha = EXCLUDED.fecha
            """,
            cita.valor, pago.referencia, pago.copago.valorPesos, pago.fecha.utc(),
        )
    }

    override fun buscar(cita: CitaId): PagoCopago? =
        fuente.consultar("SELECT referencia, valor_pesos, fecha FROM pago_copago WHERE cita = ?", cita.valor) {
            PagoCopago(it.getString("referencia"), Copago(it.getLong("valor_pesos")), requireNotNull(it.instante("fecha")))
        }.singleOrNull()
}
