package com.hospital.facturacion.data.comunicacion

import com.hospital.facturacion.data.appwrite.TablaAppwrite
import com.hospital.facturacion.data.appwrite.fechaAppwrite
import com.hospital.facturacion.data.appwrite.instanteAppwrite
import com.hospital.facturacion.domain.comunicacion.BandejaComunicaciones
import com.hospital.facturacion.domain.comunicacion.Comunicacion
import com.hospital.facturacion.domain.comunicacion.EstadoEnvio
import com.hospital.facturacion.domain.comunicacion.MotivoComunicacion
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class FilaComunicacion(
    val cita: String,
    val motivo: MotivoComunicacion,
    val destino: String,
    val texto: String,
    val estado: EstadoEnvio,
    val canal: String,
    val fecha: String,
)

/** Una fila por mensaje en `comunicaciones`, con ID aleatorio: un mensaje nunca reemplaza a otro. */
class BandejaComunicacionesAppwrite(private val tabla: TablaAppwrite<FilaComunicacion>) : BandejaComunicaciones {
    override fun registrar(comunicacion: Comunicacion) = with(comunicacion.columnas()) {
        tabla.guardar(UUID.randomUUID().toString().replace("-", ""), FilaComunicacion(cita, motivo, destino, texto, estado, canal, fecha.fechaAppwrite()))
    }

    override fun recientes(): List<Comunicacion> = tabla.buscar().sortedByDescending { it.fecha }.map {
        ColumnasComunicacion(it.cita, it.motivo, it.destino, it.texto, it.estado, it.canal, it.fecha.instanteAppwrite()).comunicacion()
    }
}
