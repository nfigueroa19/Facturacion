package com.hospital.facturacion.domain.cita

import com.hospital.facturacion.domain.comunicacion.Celular
import java.time.LocalDate

/** `celular` lo registra el call center al asignar la cita; las citas sembradas antes de la Fase 10 no lo tienen. */
data class Cita(
    val id: CitaId,
    val identificacionPaciente: String,
    val fecha: LocalDate,
    val medico: String,
    val especialidad: String,
    val regimen: Regimen,
    val copago: Copago?,
    val celular: Celular? = null,
)
