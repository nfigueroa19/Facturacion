package com.hospital.facturacion.data.copago

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.slf4j.Logger
import java.net.http.HttpClient

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
 * `GET /v1/transactions/{id}`. El sandbox la responde sin autenticación (verificado el 2026-10-24), así que no hace
 * falta la llave privada. El registro guarda solo el código HTTP, nunca la transacción.
 */
class ClienteTransaccionesWompi(private val credenciales: CredencialesWompi, private val http: HttpClient, private val registro: Logger) :
    TransaccionesWompi {
    override fun consultar(id: String): ConsultaTransaccion {
        val respuesta = http.consultarWompi("${credenciales.base}/v1/transactions/$id", registro) ?: return ConsultaTransaccion.Falla
        return when (respuesta.statusCode()) {
            200 -> datosWompi(respuesta.body(), ::transaccionDe)?.let(ConsultaTransaccion::Encontrada) ?: ConsultaTransaccion.Falla
            404 -> ConsultaTransaccion.NoExiste
            else -> ConsultaTransaccion.Falla.also { registro.warn("Wompi respondió {}", respuesta.statusCode()) }
        }
    }
}

private fun transaccionDe(datos: JsonObject): TransaccionWompi {
    fun texto(campo: String) = datos.getValue(campo).jsonPrimitive.content
    return TransaccionWompi(
        id = texto("id"),
        estado = EstadoTransaccionWompi.valueOf(texto("status")),
        referencia = texto("reference"),
        montoCentavos = texto("amount_in_cents").toLong(),
        moneda = texto("currency"),
    )
}
