package com.hospital.facturacion.domain.comunicacion

import com.hospital.facturacion.domain.cita.Cita

/** Avisa al paciente por el canal configurado. Devuelve null si la cita no tiene celular registrado. */
fun interface NotificadorPaciente {
    fun notificar(cita: Cita, motivo: MotivoComunicacion, valores: Map<String, String>): Comunicacion?
}
