package com.hospital.facturacion.data.secretos

/** Valor sensible en `CharArray` para poder borrarlo de memoria; nunca se imprime. */
class Secreto(private val valor: CharArray) {
    fun <T> usar(bloque: (CharArray) -> T): T = bloque(valor)

    fun borrar() = valor.fill('\u0000')

    override fun toString(): String = "Secreto(****)"
}
