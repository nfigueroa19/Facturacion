package com.hospital.facturacion.data.prefactura

import com.hospital.facturacion.data.appwrite.TablaAppwrite
import com.hospital.facturacion.domain.prefactura.ConsecutivoRadicado
import com.hospital.facturacion.domain.prefactura.GeneradorConsecutivo
import kotlinx.serialization.Serializable

@Serializable
data class FilaConsecutivo(val valor: Long)

/** Contador en la fila `radicado` de `consecutivos`. Appwrite suma en el servidor: dos radicaciones no repiten número. */
class GeneradorConsecutivoAppwrite(private val consecutivos: TablaAppwrite<FilaConsecutivo>) : GeneradorConsecutivo {
    override fun siguiente(): ConsecutivoRadicado = consecutivoProvisional(consecutivos.incrementar(FILA_RADICADO, "valor").valor)
}

/** Crea el contador en 0 si no existe. Nunca lo reinicia. */
fun prepararConsecutivo(consecutivos: TablaAppwrite<FilaConsecutivo>) {
    if (consecutivos.leer(FILA_RADICADO) == null) consecutivos.guardar(FILA_RADICADO, FilaConsecutivo(0))
}

private const val FILA_RADICADO = "radicado"
