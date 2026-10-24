package com.hospital.facturacion.data.copago

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.slf4j.Logger
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.time.Duration

/** Estados de una transacción de Wompi. Todos son finales salvo PENDING. */
enum class EstadoTransaccionWompi { PENDING, APPROVED, DECLINED, VOIDED, ERROR }

data class TransaccionWompi(
    val id: String,
    val estado: EstadoTransaccionWompi,
    val referencia: String,
    val montoCentavos: Long,
    val moneda: String,
)

sealed interface ConsultaTransaccion {
    data class Encontrada(val transaccion: TransaccionWompi) : ConsultaTransaccion

    data object NoExiste : ConsultaTransaccion

    /** Wompi no respondió o respondió algo que no se pudo leer. */
    data object Falla : ConsultaTransaccion
}

fun interface TransaccionesWompi {
    fun consultar(id: String): ConsultaTransaccion
}

/**
 * `GET /v1/transactions/{id}` con la llave privada: Wompi ya no admite esta consulta sin autenticación ni con la llave
 * pública. El registro guarda solo el código HTTP, nunca la transacción.
 */
class ClienteTransaccionesWompi(private val credenciales: CredencialesWompi, private val http: HttpClient, private val registro: Logger) :
    TransaccionesWompi {
    override fun consultar(id: String): ConsultaTransaccion {
        val peticion = HttpRequest.newBuilder(URI.create("${credenciales.base}/v1/transactions/$id"))
            .timeout(Duration.ofSeconds(15))
            .header("Authorization", "Bearer ${credenciales.privada}")
            .GET()
            .build()
        val respuesta = try {
            http.send(peticion, BodyHandlers.ofString())
        } catch (e: IOException) {
            registro.warn("Wompi no respondió: {}", e.javaClass.simpleName)
            return ConsultaTransaccion.Falla
        }
        return when (respuesta.statusCode()) {
            200 -> transaccionDe(respuesta.body())?.let(ConsultaTransaccion::Encontrada) ?: ConsultaTransaccion.Falla
            404 -> ConsultaTransaccion.NoExiste
            else -> ConsultaTransaccion.Falla.also { registro.warn("Wompi respondió {}", respuesta.statusCode()) }
        }
    }
}

private fun transaccionDe(cuerpo: String): TransaccionWompi? = try {
    val datos = Json.parseToJsonElement(cuerpo).jsonObject.getValue("data").jsonObject
    fun texto(campo: String) = datos.getValue(campo).jsonPrimitive.content
    TransaccionWompi(
        id = texto("id"),
        estado = EstadoTransaccionWompi.valueOf(texto("status")),
        referencia = texto("reference"),
        montoCentavos = texto("amount_in_cents").toLong(),
        moneda = texto("currency"),
    )
} catch (e: IllegalArgumentException) {
    null
} catch (e: NoSuchElementException) {
    null
}
