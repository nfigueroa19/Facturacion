package com.hospital.facturacion.domain.comunicacion

import com.hospital.facturacion.domain.cita.CitaId
import java.time.Instant

/** Celular móvil colombiano de 10 dígitos que empieza por 3. El adaptador SMS agrega el indicativo +57. */
@JvmInline
value class Celular private constructor(val valor: String) {
    companion object {
        private val PATRON = Regex("3[0-9]{9}")

        fun de(valor: String): Celular? = valor.filter { !it.isWhitespace() }.takeIf(PATRON::matches)?.let(::Celular)
    }
}

/**
 * Por qué se le escribe al paciente: el SMS con el ID de acceso (flujo del paciente, paso 3), el consecutivo al radicar
 * (paso 9) y las novedades (RN-12).
 */
enum class MotivoComunicacion { CITA_ASIGNADA, SOLICITUD_RADICADA, NOVEDAD_REGISTRADA }

/** SIMULADO: no salió del servidor y solo se ve en el panel de comunicaciones del personal. */
enum class EstadoEnvio { ENVIADO, SIMULADO, FALLIDO }

data class MensajeSms(val destino: Celular, val texto: String)

data class ResultadoEnvio(val estado: EstadoEnvio, val canal: String)

data class Comunicacion(
    val cita: CitaId,
    val motivo: MotivoComunicacion,
    val mensaje: MensajeSms,
    val envio: ResultadoEnvio,
    val fecha: Instant,
)
