package com.hospital.facturacion.data.appwrite

import org.slf4j.Logger
import java.time.Duration

/** Falla que puede no repetirse: Appwrite no respondió, respondió 429 o 5xx, o devolvió `{}` en lugar de la fila guardada. */
class FalloTransitorioAppwrite(mensaje: String, causa: Throwable? = null) : IllegalStateException(mensaje, causa)

fun interface Pausa {
    fun esperar(duracion: Duration)
}

/**
 * Repite una operación idempotente ante [FalloTransitorioAppwrite], con espera exponencial: [esperaInicial], el doble,
 * el cuádruple... Agotados los [intentos], propaga la última falla. Cualquier otra excepción sale al primer intento.
 */
class ReintentosAppwrite(private val intentos: Int, private val esperaInicial: Duration, private val pausa: Pausa, private val registro: Logger) {
    init {
        require(intentos >= 1) { "Se necesita al menos un intento" }
    }

    fun <T> ejecutar(operacion: () -> T): T {
        var espera = esperaInicial
        for (intento in 1 until intentos) {
            try {
                return operacion()
            } catch (e: FalloTransitorioAppwrite) {
                registro.warn("Appwrite, intento {} de {}: {}. Nuevo intento en {} ms", intento, intentos, e.message, espera.toMillis())
                pausa.esperar(espera)
                espera = espera.multipliedBy(2)
            }
        }
        return operacion()
    }
}
