package com.hospital.facturacion.data.appwrite

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.net.URLEncoder
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
import kotlin.text.Charsets.UTF_8

/** Filas de una tabla de Appwrite TablesDB con la forma `F`. Las columnas de sistema (`$id`, `$createdAt`...) se ignoran. */
class TablaAppwrite<F>(private val cliente: ClienteAppwrite, baseDatos: String, tabla: String, private val forma: KSerializer<F>) {
    private val filas = "/tablesdb/$baseDatos/tables/$tabla/rows"

    fun leer(id: String): F? {
        val respuesta = cliente.enviar("$filas/$id", BodyHandlers.ofString(), 200, 404) { GET() }
        return if (respuesta.statusCode() == 404) null else formato.decodeFromString(forma, respuesta.body())
    }

    /** Crea la fila o la reemplaza completa. */
    fun guardar(id: String, fila: F) {
        val datos = buildJsonObject { put("data", formato.encodeToJsonElement(forma, fila)) }
        val guardada = cliente.enviar("$filas/$id", BodyHandlers.ofString(), 200, 201) { conJson("PUT", datos) }.body()
        // Appwrite Cloud respondió una vez 200 con `{}` sin crear la fila: solo cuenta si devuelve la fila con su ID.
        check(formato.parseToJsonElement(guardada).jsonObject["\$id"]?.jsonPrimitive?.content == id) { "Appwrite no confirmó la fila" }
    }

    /** Falla si hay más de [MAXIMO_FILAS] coincidencias en lugar de entregar una lista incompleta. */
    fun buscar(vararg consultas: ConsultaAppwrite): List<F> {
        val parametros = (consultas.toList() + ConsultaAppwrite.limite(MAXIMO_FILAS))
            .joinToString("&") { "queries%5B%5D=" + URLEncoder.encode(it.json, UTF_8) }
        val lista = formato.decodeFromString(ListaFilas.serializer(), cliente.enviar("$filas?$parametros", BodyHandlers.ofString(), 200) { GET() }.body())
        check(lista.total <= lista.rows.size) { "La consulta supera $MAXIMO_FILAS filas" }
        return lista.rows.map { formato.decodeFromJsonElement(forma, it) }
    }

    /** Suma 1 a una columna numérica en el servidor, sin carreras entre peticiones, y devuelve la fila resultante. */
    fun incrementar(id: String, columna: String): F {
        val suma = buildJsonObject { put("value", 1) }
        return formato.decodeFromString(forma, cliente.enviar("$filas/$id/$columna/increment", BodyHandlers.ofString(), 200) { conJson("PATCH", suma) }.body())
    }
}

const val MAXIMO_FILAS = 5000

@Serializable
private class ListaFilas(val total: Long, val rows: List<JsonObject>)

// Las columnas opcionales viajan como null para que reemplazar una fila también borre sus valores anteriores.
private val formato = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

private fun HttpRequest.Builder.conJson(metodo: String, cuerpo: JsonObject) =
    header("Content-Type", "application/json").method(metodo, BodyPublishers.ofString(cuerpo.toString()))
