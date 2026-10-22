package com.hospital.facturacion.domain.cita

import java.time.LocalDate

data class Cita(
    val id: CitaId,
    val identificacionPaciente: String,
    val fecha: LocalDate,
    val medico: String,
    val especialidad: String,
    val regimen: Regimen,
    val copago: Copago?,
)
