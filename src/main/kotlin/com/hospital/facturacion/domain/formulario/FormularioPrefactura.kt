package com.hospital.facturacion.domain.formulario

import com.hospital.facturacion.domain.prefactura.DatosFacturacion

data class CampoFormulario(
    val clave: String,
    val etiqueta: String,
    val tipo: TipoCampo,
    val obligatorio: Boolean,
    val ayuda: String? = null,
) {
    fun error(valor: String): ErrorCampo? = when {
        valor.isEmpty() -> ErrorCampo.FALTA.takeIf { obligatorio }
        valor.length > LARGO_MAXIMO -> ErrorCampo.LARGO
        !tipo.admite(valor) -> ErrorCampo.FORMATO
        else -> null
    }

    companion object {
        const val LARGO_MAXIMO = 120
    }
}

enum class ErrorCampo { FALTA, FORMATO, LARGO }

sealed interface ResultadoDatos {
    data class Validos(val datos: DatosFacturacion) : ResultadoDatos

    data class Invalidos(val errores: Map<String, ErrorCampo>) : ResultadoDatos
}

/** Campos de `config/formulario-prefactura.json`. La lista completa sigue abierta (P-01). */
class FormularioPrefactura(val campos: List<CampoFormulario>) {
    init {
        require(campos.isNotEmpty()) { "El formulario no tiene campos" }
        require(campos.distinctBy { it.clave }.size == campos.size) { "El formulario repite claves de campo" }
    }

    /** Ignora claves que no están en el formulario y descarta los opcionales vacíos. */
    fun validar(valores: Map<String, String>): ResultadoDatos {
        val limpios = campos.associate { it.clave to valores[it.clave].orEmpty().trim() }
        val errores = campos.mapNotNull { campo -> campo.error(limpios.getValue(campo.clave))?.let { campo.clave to it } }
        if (errores.isNotEmpty()) return ResultadoDatos.Invalidos(errores.toMap())
        return ResultadoDatos.Validos(DatosFacturacion(limpios.filterValues { it.isNotEmpty() }))
    }
}
