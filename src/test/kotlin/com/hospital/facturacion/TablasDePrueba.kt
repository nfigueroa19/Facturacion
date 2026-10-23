package com.hospital.facturacion

import com.sun.net.httpserver.HttpExchange
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import java.net.URLDecoder
import java.util.concurrent.ConcurrentHashMap
import kotlin.text.Charsets.UTF_8

/**
 * Filas de TablesDB en memoria, por tabla e ID, con las operaciones que usa `TablaAppwrite`: leer, reemplazar (PUT),
 * listar con `equal`, `orderAsc` y `limit`, e incrementar. Con [confirmarGuardado] en falso, PUT responde `{}`.
 */
class TablasDePrueba {
    val filas = ConcurrentHashMap<String, MutableMap<String, JsonObject>>()
    var confirmarGuardado = true

    fun de(tabla: String): MutableMap<String, JsonObject> = filas.getOrPut(tabla) { ConcurrentHashMap() }

    fun responder(intercambio: HttpExchange, ruta: String): Pair<Int, ByteArray?> {
        val partes = ruta.split('/')
        val tabla = de(partes[0])
        val id = partes.getOrNull(2) ?: return if (intercambio.requestMethod == "GET") 200 to tabla.listar(intercambio.requestURI.rawQuery) else 405 to null
        val cuerpo = intercambio.requestBody.readAllBytes().decodeToString()
        return when (intercambio.requestMethod) {
            "GET" -> tabla[id]?.let { 200 to it.conId(id) } ?: (404 to null)
            "PUT" -> 200 to Json.parseToJsonElement(cuerpo).jsonObject.getValue("data").jsonObject.also { tabla[id] = it }
                .let { if (confirmarGuardado) it.conId(id) else "{}".toByteArray() }
            "PATCH" -> tabla[id]?.let { 200 to it.incrementado(partes[3], cuerpo).also { fila -> tabla[id] = fila }.conId(id) } ?: (404 to null)
            else -> 405 to null
        }
    }
}

private fun JsonObject.conId(id: String) = JsonObject(this + ("\$id" to JsonPrimitive(id))).toString().toByteArray()

private fun JsonObject.incrementado(columna: String, cuerpo: String): JsonObject {
    val suma = Json.parseToJsonElement(cuerpo).jsonObject.getValue("value").jsonPrimitive.long
    return JsonObject(this + (columna to JsonPrimitive(getValue(columna).jsonPrimitive.long + suma)))
}

private fun Map<String, JsonObject>.listar(consulta: String?): ByteArray {
    val consultas = consulta.orEmpty().split('&').filter { it.startsWith("queries%5B%5D=") }
        .map { Json.parseToJsonElement(URLDecoder.decode(it.substringAfter('='), UTF_8)).jsonObject }
    fun JsonObject.valor(columna: String) = getValue(columna).jsonPrimitive.content
    var filas = values.toList()
    consultas.filter { it.metodo == "equal" }.forEach { q ->
        filas = filas.filter { fila -> q.valores.any { it.jsonPrimitive.content == fila.valor(q.columna) } }
    }
    consultas.filter { it.metodo == "orderAsc" }.forEach { q -> filas = filas.sortedBy { it.valor(q.columna) } }
    val limite = consultas.single { it.metodo == "limit" }.valores.single().jsonPrimitive.int
    return buildJsonObject {
        put("total", JsonPrimitive(filas.size))
        put("rows", JsonArray(filas.take(limite)))
    }.toString().toByteArray()
}

private val JsonObject.metodo get() = getValue("method").jsonPrimitive.content
private val JsonObject.columna get() = getValue("attribute").jsonPrimitive.content
private val JsonObject.valores get() = getValue("values").jsonArray
