package com.hospital.facturacion.data.secretos

interface FuenteSecretos {
    fun aliases(): Set<String>

    fun leer(alias: String): Secreto?
}
