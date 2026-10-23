package com.hospital.facturacion.data.cita

import com.hospital.facturacion.domain.cita.AgendaCitas
import com.hospital.facturacion.domain.cita.Cita
import com.hospital.facturacion.domain.cita.CitaId
import java.security.MessageDigest

data class CitaRegistrada(val cita: Cita, val codigoAcceso: String)

/** Agenda de prueba. En producción la reemplaza el adaptador al sistema del call center o a Dinámica (P-12). */
class AgendaCitasMemoria(private val citas: List<CitaRegistrada>) : AgendaCitas {
    override fun buscar(id: CitaId): Cita? = citas.firstOrNull { it.cita.id == id }?.cita

    override fun buscarPorAcceso(identificacion: String, codigoAcceso: String): Cita? = citas
        .firstOrNull { it.cita.identificacionPaciente == identificacion && it.codigoAcceso.igualA(codigoAcceso) }
        ?.cita
}

// Comparación en tiempo constante para no filtrar por tiempo de respuesta cuántos caracteres coinciden.
internal fun String.igualA(otro: String): Boolean = MessageDigest.isEqual(toByteArray(), otro.toByteArray())
