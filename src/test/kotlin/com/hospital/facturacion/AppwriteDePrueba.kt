package com.hospital.facturacion

import com.hospital.facturacion.arranque.ALIAS_APPWRITE_CLAVE
import com.hospital.facturacion.arranque.ALIAS_APPWRITE_ENDPOINT
import com.hospital.facturacion.arranque.ALIAS_APPWRITE_PROYECTO
import com.hospital.facturacion.data.appwrite.BucketAppwrite
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.net.URI
import java.util.concurrent.ConcurrentHashMap

/**
 * Imitación local de Appwrite Storage: un bucket `soportes` en memoria con las rutas y los códigos de la API REST
 * (201 al crear, 409 si el ID existe, 204 al borrar, 404 si no está, 401 sin proyecto o clave correctos).
 */
class AppwriteDePrueba(val proyecto: String = "proyecto-prueba", val claveApi: String = "clave-api-prueba") : AutoCloseable {
    val archivos = ConcurrentHashMap<String, ArchivoAppwrite>()
    private val servidor = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
        createContext("/v1/storage/buckets/soportes/files") { intercambio -> intercambio.use { atender(it) } }
        start()
    }

    val endpoint: URI get() = URI("http://127.0.0.1:${servidor.address.port}/v1")

    fun bucket(clave: String = claveApi) = BucketAppwrite(endpoint, proyecto, "soportes", clave)

    /** Alias del almacén PKCS12 que apuntan a esta imitación. */
    fun secretos() = mapOf(
        ALIAS_APPWRITE_ENDPOINT to endpoint.toString(),
        ALIAS_APPWRITE_PROYECTO to proyecto,
        ALIAS_APPWRITE_CLAVE to claveApi,
    )

    private fun atender(intercambio: HttpExchange) {
        val cabeceras = intercambio.requestHeaders
        val autorizado = cabeceras.getFirst("X-Appwrite-Project") == proyecto && cabeceras.getFirst("X-Appwrite-Key") == claveApi
        val ruta = intercambio.requestURI.path.substringAfter("/files").trim('/')
        val (estado, cuerpo) = if (autorizado) archivos.responder(intercambio.requestMethod, ruta, intercambio) else 401 to null
        intercambio.sendResponseHeaders(estado, cuerpo?.size?.toLong() ?: -1)
        cuerpo?.let(intercambio.responseBody::write)
    }

    override fun close() = servidor.stop(0)
}

data class ArchivoAppwrite(val nombre: String, val contenido: ByteArray)

private fun MutableMap<String, ArchivoAppwrite>.responder(metodo: String, ruta: String, intercambio: HttpExchange): Pair<Int, ByteArray?> {
    val id = ruta.substringBefore('/')
    return when {
        metodo == "POST" -> crear(intercambio)
        metodo == "DELETE" -> (if (remove(id) != null) 204 else 404) to null
        metodo == "GET" && ruta.endsWith("/download") -> get(id)?.let { 200 to it.contenido } ?: (404 to null)
        metodo == "GET" -> if (id in this) 200 to "{}".toByteArray() else 404 to null
        else -> 405 to null
    }
}

// Lee el multipart como ISO-8859-1, que conserva cada byte; los PDF de prueba son texto.
private fun MutableMap<String, ArchivoAppwrite>.crear(intercambio: HttpExchange): Pair<Int, ByteArray?> {
    val limite = intercambio.requestHeaders.getFirst("Content-Type").substringAfter("boundary=")
    val partes = intercambio.requestBody.readAllBytes().toString(Charsets.ISO_8859_1).split("--$limite")
        .filter { it.contains("Content-Disposition") }
        .associate { parte ->
            val (cabecera, valor) = parte.removePrefix("\r\n").removeSuffix("\r\n").split("\r\n\r\n", limit = 2)
            cabecera.atributo("name") to (cabecera to valor)
        }
    val id = partes.getValue("fileId").second
    if (id in this) return 409 to null
    val (cabecera, contenido) = partes.getValue("file")
    this[id] = ArchivoAppwrite(cabecera.atributo("filename"), contenido.toByteArray(Charsets.ISO_8859_1))
    return 201 to "{}".toByteArray()
}

private fun String.atributo(nombre: String): String =
    requireNotNull(Regex("""\b$nombre="([^"]+)"""").find(this)) { "Falta $nombre en la parte multipart" }.groupValues[1]
