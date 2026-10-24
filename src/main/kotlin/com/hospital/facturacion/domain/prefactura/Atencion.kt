package com.hospital.facturacion.domain.prefactura

import java.time.Instant

/**
 * Número que entrega Dinámica: el ID de ingreso o el número de factura. El formato real no está documentado (P-13);
 * se aceptan letras, números y guiones. Mientras no haya integración, el facturador lo copia desde Dinámica.
 */
@JvmInline
value class NumeroDinamica private constructor(val valor: String) {
    companion object {
        private val PATRON = Regex("[A-Za-z0-9-]{1,30}")

        fun de(valor: String): NumeroDinamica? = valor.trim().takeIf(PATRON::matches)?.let(::NumeroDinamica)
    }
}

/** Texto libre del facturador: motivo de anulación o error que devolvió Dinámica al facturar. */
@JvmInline
value class Observacion private constructor(val texto: String) {
    companion object {
        fun de(texto: String): Observacion? =
            texto.trim().takeIf { it.isNotEmpty() && it.length <= Novedad.LARGO_MAXIMO }?.let(::Observacion)
    }
}

data class Registro<T>(val valor: T, val facturador: String, val fecha: Instant)

/** Lo que pasa con la solicitud desde el día de la cita: ingreso, intentos de facturación, factura o anulación. */
data class Atencion(
    val ingreso: Registro<NumeroDinamica>? = null,
    val intentosFallidos: List<Registro<Observacion>> = emptyList(),
    val factura: Registro<NumeroDinamica>? = null,
    val anulacion: Registro<Observacion>? = null,
)

/** Reglas de atención que no son transiciones de estado. */
enum class MotivoRechazoAtencion {
    ESTADO_NO_PERMITE,
    SIN_INGRESO,
    INGRESO_YA_REGISTRADO,
    FUERA_DEL_DIA_DE_LA_CITA,
}
