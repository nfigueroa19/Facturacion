package com.hospital.facturacion.data.cita

import com.hospital.facturacion.domain.cita.AgendaCitas
import com.hospital.facturacion.domain.cita.Cita
import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.cita.RegistroCitas
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

data class CitaRegistrada(val cita: Cita, val codigoAcceso: String)

/**
 * Agenda de prueba. Las citas del call center se pierden al reiniciar (destino PostgreSQL, solo local).
 * En producción la reemplaza el adaptador al sistema del call center o a Dinámica (P-12).
 */
class AgendaCitasMemoria(iniciales: List<CitaRegistrada>) : AgendaCitas, RegistroCitas {
    private val citas = ConcurrentHashMap(iniciales.associateBy { it.cita.id })

    override fun registrar(cita: Cita, codigoAcceso: String) {
        citas[cita.id] = CitaRegistrada(cita, codigoAcceso)
    }

    override fun buscar(id: CitaId): Cita? = citas[id]?.cita

    override fun buscarPorAcceso(identificacion: String, codigoAcceso: String): Cita? = citas.values
        .firstOrNull { it.cita.identificacionPaciente == identificacion && it.codigoAcceso.igualA(codigoAcceso) }
        ?.cita

    override fun buscarPorIdentificacion(identificacion: String): List<Cita> =
        citas.values.map { it.cita }.filter { it.identificacionPaciente == identificacion }
}

// Comparación en tiempo constante para no filtrar por tiempo de respuesta cuántos caracteres coinciden.
internal fun String.igualA(otro: String): Boolean = MessageDigest.isEqual(toByteArray(), otro.toByteArray())
