package com.hospital.facturacion.data.cita

import com.hospital.facturacion.data.configuracion.leerArchivo
import com.hospital.facturacion.domain.cita.Cita
import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.cita.Copago
import com.hospital.facturacion.domain.cita.Regimen
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.nio.file.Path
import java.time.Clock
import java.time.LocalDate

/** Forma de `config/citas-prueba.json`. La fecha va relativa al día de arranque para que la prueba no caduque. */
@Serializable
data class CitaPruebaJson(
    val id: String,
    val identificacion: String,
    val codigoAcceso: String,
    val diasDesdeHoy: Long,
    val medico: String,
    val especialidad: String,
    val regimen: Regimen,
    val copagoPesos: Long? = null,
) {
    fun aDominio(hoy: LocalDate): CitaRegistrada = CitaRegistrada(
        Cita(
            id = requireNotNull(CitaId.de(id)) { "Identificador de cita no permitido: '$id'" },
            identificacionPaciente = identificacion,
            fecha = hoy.plusDays(diasDesdeHoy),
            medico = medico,
            especialidad = especialidad,
            regimen = regimen,
            copago = copagoPesos?.let(::Copago),
        ),
        codigoAcceso,
    )
}

fun leerCitasPrueba(archivo: Path, json: Json, reloj: Clock): List<CitaRegistrada> =
    json.leerArchivo<List<CitaPruebaJson>>(archivo).map { it.aDominio(LocalDate.now(reloj)) }
