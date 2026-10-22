package com.hospital.facturacion.data.sesion

import com.hospital.facturacion.domain.sesion.Sesiones
import com.hospital.facturacion.domain.sesion.TokenSesion
import java.security.SecureRandom
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap

/** Sesiones con token aleatorio de 256 bits. Se pierden al reiniciar el servidor. */
class SesionesMemoria<T : Any>(
    private val reloj: Clock,
    private val duracion: Duration,
    private val aleatorio: SecureRandom,
) : Sesiones<T> {
    private class Sesion<T>(val sujeto: T, val vence: Instant)

    private val sesiones = ConcurrentHashMap<TokenSesion, Sesion<T>>()

    override fun abrir(sujeto: T): TokenSesion {
        val bytes = ByteArray(32).also(aleatorio::nextBytes)
        val token = TokenSesion(Base64.getUrlEncoder().withoutPadding().encodeToString(bytes))
        sesiones[token] = Sesion(sujeto, reloj.instant().plus(duracion))
        return token
    }

    override fun sujeto(token: TokenSesion): T? {
        val sesion = sesiones[token] ?: return null
        if (reloj.instant().isBefore(sesion.vence)) return sesion.sujeto
        sesiones.remove(token)
        return null
    }
}
