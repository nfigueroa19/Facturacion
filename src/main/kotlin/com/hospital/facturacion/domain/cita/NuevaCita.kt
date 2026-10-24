package com.hospital.facturacion.domain.cita

import com.hospital.facturacion.domain.comunicacion.Celular
import java.time.LocalDate
import java.time.format.DateTimeParseException

/** Lo que el agente del call center escribe al asignar una cita. Solo datos ficticios durante el piloto. */
data class DatosNuevaCita(
    val identificacion: String,
    val celular: String,
    val fecha: String,
    val medico: String,
    val especialidad: String,
    val regimen: Regimen,
    val copagoPesos: Long?,
)

enum class ErrorNuevaCita { IDENTIFICACION, CELULAR, FECHA, MEDICO, ESPECIALIDAD, COPAGO }

sealed interface ValidacionNuevaCita {
    data class Valida(val cita: Cita) : ValidacionNuevaCita

    data class Invalida(val errores: Set<ErrorNuevaCita>) : ValidacionNuevaCita
}

/** Formato técnico de cada dato. No se puede asignar una cita en un día que ya pasó. */
fun DatosNuevaCita.validar(hoy: LocalDate, id: CitaId): ValidacionNuevaCita {
    val identificacionLimpia = identificacion.trim().takeIf(PATRON_IDENTIFICACION::matches)
    val celularValido = Celular.de(celular)
    val dia = fechaDe(fecha)?.takeIf { !it.isBefore(hoy) }
    val errores = buildSet {
        if (identificacionLimpia == null) add(ErrorNuevaCita.IDENTIFICACION)
        if (celularValido == null) add(ErrorNuevaCita.CELULAR)
        if (dia == null) add(ErrorNuevaCita.FECHA)
        if (medico.trim().length !in 1..LARGO_MAXIMO) add(ErrorNuevaCita.MEDICO)
        if (especialidad.trim().length !in 1..LARGO_MAXIMO) add(ErrorNuevaCita.ESPECIALIDAD)
        if (copagoPesos != null && copagoPesos <= 0) add(ErrorNuevaCita.COPAGO)
    }
    if (errores.isNotEmpty() || identificacionLimpia == null || dia == null) return ValidacionNuevaCita.Invalida(errores)
    return ValidacionNuevaCita.Valida(
        Cita(id, identificacionLimpia, dia, medico.trim(), especialidad.trim(), regimen, copagoPesos?.let(::Copago), celularValido),
    )
}

private const val LARGO_MAXIMO = 120
private val PATRON_IDENTIFICACION = Regex("[0-9A-Za-z]{4,20}")

private fun fechaDe(valor: String): LocalDate? = try {
    LocalDate.parse(valor.trim())
} catch (e: DateTimeParseException) {
    null
}
