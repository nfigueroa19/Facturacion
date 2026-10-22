package com.hospital.facturacion.service.paciente

import com.hospital.facturacion.domain.cita.AgendaCitas
import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.sesion.Sesiones
import com.hospital.facturacion.domain.sesion.TokenSesion

/** Identificación + ID de acceso enviado por SMS (RN-05). La vigencia del ID sigue abierta (P-09). */
class IniciarSesionPaciente(private val agenda: AgendaCitas, private val sesiones: Sesiones<CitaId>) {
    fun ejecutar(identificacion: String, codigoAcceso: String): TokenSesion? =
        agenda.buscarPorAcceso(identificacion.trim(), codigoAcceso.trim())?.let { sesiones.abrir(it.id) }
}
