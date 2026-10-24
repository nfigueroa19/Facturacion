package com.hospital.facturacion.data.digiturno

import com.hospital.facturacion.data.appwrite.TablaAppwrite
import com.hospital.facturacion.data.postgres.consultar
import com.hospital.facturacion.data.prefactura.FilaConsecutivo
import com.hospital.facturacion.domain.digiturno.DestinoTurno
import com.hospital.facturacion.domain.digiturno.GeneradorTurnos
import com.hospital.facturacion.domain.digiturno.Turno
import com.hospital.facturacion.domain.digiturno.turnoProvisional
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import javax.sql.DataSource

class GeneradorTurnosMemoria : GeneradorTurnos {
    private val ultimos = ConcurrentHashMap<DestinoTurno, AtomicLong>()

    override fun siguiente(destino: DestinoTurno): Turno =
        turnoProvisional(destino, ultimos.getOrPut(destino) { AtomicLong() }.incrementAndGet())
}

/** Secuencias `turno_preconsulta` y `turno_facturacion` de la migración V2. */
class GeneradorTurnosPostgres(private val fuente: DataSource) : GeneradorTurnos {
    override fun siguiente(destino: DestinoTurno): Turno {
        val secuencia = if (destino == DestinoTurno.PRECONSULTA) "turno_preconsulta" else "turno_facturacion"
        return turnoProvisional(destino, fuente.consultar("SELECT nextval('$secuencia')") { it.getLong(1) }.single())
    }
}

/** Contadores en las filas `turno-preconsulta` y `turno-facturacion` de `consecutivos`, sumados en el servidor. */
class GeneradorTurnosAppwrite(private val consecutivos: TablaAppwrite<FilaConsecutivo>) : GeneradorTurnos {
    override fun siguiente(destino: DestinoTurno): Turno =
        turnoProvisional(destino, consecutivos.incrementar(filaDe(destino), "valor").valor)
}

/** Crea los contadores en 0 si no existen. Nunca los reinicia. */
fun prepararTurnos(consecutivos: TablaAppwrite<FilaConsecutivo>) = DestinoTurno.entries.map(::filaDe).forEach { fila ->
    if (consecutivos.leer(fila) == null) consecutivos.guardar(fila, FilaConsecutivo(0))
}

private fun filaDe(destino: DestinoTurno) = "turno-" + destino.name.lowercase()
