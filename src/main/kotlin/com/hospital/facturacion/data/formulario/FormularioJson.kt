package com.hospital.facturacion.data.formulario

import com.hospital.facturacion.data.configuracion.leerArchivo
import com.hospital.facturacion.domain.formulario.CampoFormulario
import com.hospital.facturacion.domain.formulario.FormularioPrefactura
import com.hospital.facturacion.domain.formulario.TipoCampo
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.nio.file.Path

/** Forma de cada campo en `config/formulario-prefactura.json`. */
@Serializable
data class CampoJson(
    val clave: String,
    val etiqueta: String,
    val tipo: TipoCampo = TipoCampo.TEXTO,
    val obligatorio: Boolean = true,
    val ayuda: String? = null,
) {
    fun aDominio(): CampoFormulario {
        require(PATRON_CLAVE.matches(clave)) { "Clave de campo no permitida: '$clave'" }
        require(etiqueta.isNotBlank()) { "El campo '$clave' no tiene etiqueta" }
        return CampoFormulario(clave, etiqueta.trim(), tipo, obligatorio, ayuda?.trim()?.ifEmpty { null })
    }
}

// Fuera de la clase: el plugin de serialización usa el companion de CampoJson para su serializador.
private val PATRON_CLAVE = Regex("[a-z0-9]+(-[a-z0-9]+)*")

/** Se lee al arrancar: un formulario mal escrito detiene el servidor con el motivo, en lugar de mostrar un formulario incompleto. */
fun leerFormulario(archivo: Path, json: Json): FormularioPrefactura =
    FormularioPrefactura(json.leerArchivo<List<CampoJson>>(archivo).map(CampoJson::aDominio))
