package com.hospital.facturacion

import com.hospital.facturacion.arranque.ALIAS_APPWRITE_CLAVE
import com.hospital.facturacion.arranque.ALIAS_APPWRITE_ENDPOINT
import com.hospital.facturacion.arranque.ALIAS_APPWRITE_PROYECTO
import com.hospital.facturacion.data.appwrite.ClienteAppwrite
import com.hospital.facturacion.data.appwrite.ReintentosAppwrite
import com.hospital.facturacion.data.appwrite.TablaAppwrite
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import kotlinx.serialization.KSerializer
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.net.URI
import java.net.http.HttpClient
import java.time.Duration
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

const val BASE_APPWRITE_PRUEBA = "facturacion_db"
const val BUCKET_APPWRITE_PRUEBA = "soportes_pdf"

/**
 * Imitación local de Appwrite: el bucket de Storage y las tablas de TablesDB en memoria, con las rutas y los códigos
 * de la API REST. Responde 401 sin el proyecto o la clave correctos.
 */
class AppwriteDePrueba(val proyecto: String = "proyecto-prueba", val claveApi: String = "clave-api-prueba") : AutoCloseable {
    val archivos = ConcurrentHashMap<String, ArchivoAppwrite>()
    val tablas = TablasDePrueba()

    /** Esperas que pidieron los reintentos; no duermen para que las pruebas no se demoren. */
    val esperas: MutableList<Duration> = Collections.synchronizedList(mutableListOf())
    private val http = HttpClient.newHttpClient()
    private val servidor = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
        createContext("/v1/storage/buckets/$BUCKET_APPWRITE_PRUEBA/files") { atender(it) { ruta -> archivos.responder(it.requestMethod, ruta, it) } }
        createContext("/v1/tablesdb/$BASE_APPWRITE_PRUEBA/tables") { atender(it) { ruta -> tablas.responder(it, ruta) } }
        start()
    }

    val endpoint: URI get() = URI("http://127.0.0.1:${servidor.address.port}/v1")

    fun cliente(clave: String = claveApi) = ClienteAppwrite(endpoint, proyecto, clave, http)

    fun <F> tabla(nombre: String, forma: KSerializer<F>) = TablaAppwrite(cliente(), BASE_APPWRITE_PRUEBA, nombre, forma, reintentos())

    fun reintentos(intentos: Int = 3) = ReintentosAppwrite(intentos, Duration.ofMillis(100), esperas::add, LoggerFactory.getLogger("pruebas"))

    /** Alias del almacén PKCS12 que apuntan a esta imitación. */
    fun secretos() = mapOf(ALIAS_APPWRITE_ENDPOINT to endpoint.toString(), ALIAS_APPWRITE_PROYECTO to proyecto, ALIAS_APPWRITE_CLAVE to claveApi)

    private fun atender(intercambio: HttpExchange, responder: (ruta: String) -> Pair<Int, ByteArray?>) = intercambio.use {
        val cabeceras = it.requestHeaders
        val autorizado = cabeceras.getFirst("X-Appwrite-Project") == proyecto && cabeceras.getFirst("X-Appwrite-Key") == claveApi
        val ruta = it.requestURI.path.removePrefix(it.httpContext.path).trim('/')
        val (estado, cuerpo) = if (autorizado) responder(ruta) else 401 to null
        it.sendResponseHeaders(estado, cuerpo?.size?.toLong() ?: -1)
        cuerpo?.let(it.responseBody::write)
    }

    override fun close() {
        servidor.stop(0)
        http.close()
    }
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
