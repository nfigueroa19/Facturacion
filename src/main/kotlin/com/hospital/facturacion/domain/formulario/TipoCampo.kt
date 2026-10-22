package com.hospital.facturacion.domain.formulario

import java.time.LocalDate
import java.time.format.DateTimeParseException

/** Formato técnico del valor. No codifica reglas de negocio sobre el dato. */
enum class TipoCampo {
    TEXTO {
        override fun admite(valor: String) = true
    },
    FECHA {
        override fun admite(valor: String) = try {
            LocalDate.parse(valor)
            true
        } catch (e: DateTimeParseException) {
            false
        }
    },
    CORREO {
        override fun admite(valor: String) = PATRON_CORREO.matches(valor)
    },
    TELEFONO {
        override fun admite(valor: String) = PATRON_TELEFONO.matches(valor)
    };

    abstract fun admite(valor: String): Boolean
}

private val PATRON_CORREO = Regex("""[^@\s]+@[^@\s]+\.[^@\s]+""")
private val PATRON_TELEFONO = Regex("""\+?[0-9][0-9 ]{5,18}[0-9]""")
