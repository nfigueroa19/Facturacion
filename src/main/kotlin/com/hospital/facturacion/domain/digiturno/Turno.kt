package com.hospital.facturacion.domain.digiturno

/** Con validación aprobada el paciente pasa directo a preconsulta; si no, a facturación en ventanilla (RN-11). */
enum class DestinoTurno { PRECONSULTA, FACTURACION }

/** Por qué el digiturno envía al paciente a ventanilla (flujo del paciente, rutas a ventanilla). */
enum class MotivoVentanilla { SIN_CITA_HOY, SIN_RADICAR, EN_REVISION, CON_NOVEDADES, INGRESO_ANULADO }

data class Turno(val codigo: String, val destino: DestinoTurno)

fun interface GeneradorTurnos {
    fun siguiente(destino: DestinoTurno): Turno
}

/** Formato provisional `P-001` o `F-001`: el formato del digiturno del hospital no está documentado (P-15). */
fun turnoProvisional(destino: DestinoTurno, numero: Long): Turno {
    val prefijo = if (destino == DestinoTurno.PRECONSULTA) "P" else "F"
    return Turno("%s-%03d".format(prefijo, numero), destino)
}
