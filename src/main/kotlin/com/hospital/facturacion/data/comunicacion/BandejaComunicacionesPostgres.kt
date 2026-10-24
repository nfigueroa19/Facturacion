package com.hospital.facturacion.data.comunicacion

import com.hospital.facturacion.data.postgres.consultar
import com.hospital.facturacion.data.postgres.ejecutar
import com.hospital.facturacion.data.postgres.instante
import com.hospital.facturacion.data.postgres.utc
import com.hospital.facturacion.domain.comunicacion.BandejaComunicaciones
import com.hospital.facturacion.domain.comunicacion.Comunicacion
import com.hospital.facturacion.domain.comunicacion.EstadoEnvio
import com.hospital.facturacion.domain.comunicacion.MotivoComunicacion
import javax.sql.DataSource

class BandejaComunicacionesPostgres(private val fuente: DataSource) : BandejaComunicaciones {
    override fun registrar(comunicacion: Comunicacion) {
        val c = comunicacion.columnas()
        fuente.ejecutar(
            "INSERT INTO comunicacion (cita, motivo, destino, texto, estado, canal, fecha) VALUES (?, ?, ?, ?, ?, ?, ?)",
            c.cita, c.motivo.name, c.destino, c.texto, c.estado.name, c.canal, c.fecha.utc(),
        )
    }

    override fun recientes(): List<Comunicacion> = fuente.consultar("SELECT * FROM comunicacion ORDER BY fecha DESC LIMIT 500") {
        ColumnasComunicacion(
            it.getString("cita"),
            MotivoComunicacion.valueOf(it.getString("motivo")),
            it.getString("destino"),
            it.getString("texto"),
            EstadoEnvio.valueOf(it.getString("estado")),
            it.getString("canal"),
            requireNotNull(it.instante("fecha")),
        ).comunicacion()
    }
}
