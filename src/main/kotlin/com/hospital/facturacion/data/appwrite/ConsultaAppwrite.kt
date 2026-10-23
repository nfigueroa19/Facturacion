package com.hospital.facturacion.data.appwrite

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Consulta de TablesDB en el formato JSON que espera el parámetro `queries[]` de la API REST. */
class ConsultaAppwrite private constructor(val json: String) {
    companion object {
        fun igual(columna: String, valor: String) = de("equal", columna, JsonPrimitive(valor))

        fun ascendente(columna: String) = de("orderAsc", columna)

        fun limite(filas: Int) = de("limit", null, JsonPrimitive(filas))

        private fun de(metodo: String, columna: String?, vararg valores: JsonPrimitive) = ConsultaAppwrite(
            buildJsonObject {
                put("method", metodo)
                columna?.let { put("attribute", it) }
                put("values", JsonArray(valores.toList()))
            }.toString(),
        )
    }
}
