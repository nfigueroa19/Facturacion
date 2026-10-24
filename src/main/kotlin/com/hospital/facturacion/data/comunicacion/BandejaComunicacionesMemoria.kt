package com.hospital.facturacion.data.comunicacion

import com.hospital.facturacion.domain.comunicacion.BandejaComunicaciones
import com.hospital.facturacion.domain.comunicacion.Comunicacion
import java.util.concurrent.CopyOnWriteArrayList

/** Doble para las pruebas de los casos de uso. */
class BandejaComunicacionesMemoria : BandejaComunicaciones {
    private val comunicaciones = CopyOnWriteArrayList<Comunicacion>()

    override fun registrar(comunicacion: Comunicacion) {
        comunicaciones += comunicacion
    }

    override fun recientes(): List<Comunicacion> = comunicaciones.sortedByDescending { it.fecha }
}
