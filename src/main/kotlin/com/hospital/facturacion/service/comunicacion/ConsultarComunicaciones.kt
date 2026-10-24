package com.hospital.facturacion.service.comunicacion

import com.hospital.facturacion.domain.comunicacion.BandejaComunicaciones
import com.hospital.facturacion.domain.comunicacion.Comunicacion

/** Panel de auditoría de comunicaciones del personal: los últimos mensajes, del más reciente al más antiguo. */
class ConsultarComunicaciones(private val bandeja: BandejaComunicaciones) {
    fun ejecutar(): List<Comunicacion> = bandeja.recientes().take(MAXIMO)

    private companion object {
        const val MAXIMO = 100
    }
}
