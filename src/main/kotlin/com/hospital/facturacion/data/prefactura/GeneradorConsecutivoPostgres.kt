package com.hospital.facturacion.data.prefactura

import com.hospital.facturacion.data.postgres.consultar
import com.hospital.facturacion.domain.prefactura.ConsecutivoRadicado
import com.hospital.facturacion.domain.prefactura.GeneradorConsecutivo
import javax.sql.DataSource

/** La secuencia sobrevive a los reinicios: un consecutivo nunca se repite. */
class GeneradorConsecutivoPostgres(private val fuente: DataSource) : GeneradorConsecutivo {
    override fun siguiente(): ConsecutivoRadicado =
        consecutivoProvisional(fuente.consultar("SELECT nextval('consecutivo_radicado')") { it.getLong(1) }.single())
}
