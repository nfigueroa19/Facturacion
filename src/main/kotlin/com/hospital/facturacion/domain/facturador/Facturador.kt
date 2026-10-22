package com.hospital.facturacion.domain.facturador

data class Facturador(val usuario: String, val nombre: String)

fun interface DirectorioFacturadores {
    fun autenticar(usuario: String, clave: String): Facturador?
}
