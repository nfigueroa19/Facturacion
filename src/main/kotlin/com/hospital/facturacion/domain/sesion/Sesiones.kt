package com.hospital.facturacion.domain.sesion

@JvmInline
value class TokenSesion(val valor: String)

interface Sesiones<T : Any> {
    fun abrir(sujeto: T): TokenSesion

    /** Devuelve null si el token no existe o ya venció. */
    fun sujeto(token: TokenSesion): T?
}
