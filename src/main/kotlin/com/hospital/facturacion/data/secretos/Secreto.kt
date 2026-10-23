package com.hospital.facturacion.data.secretos

/** Valor sensible en `CharArray` para poder borrarlo de memoria; nunca se imprime. */
class Secreto(private val valor: CharArray) {
    fun <T> usar(bloque: (CharArray) -> T): T = bloque(valor)

    fun borrar() = valor.fill('\u0000')

    /** Usa el valor una sola vez y lo borra de memoria aunque el bloque falle. */
    fun <T> consumir(bloque: (CharArray) -> T): T = try {
        bloque(valor)
    } finally {
        borrar()
    }

    override fun toString(): String = "Secreto(****)"
}
