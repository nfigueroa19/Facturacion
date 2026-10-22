package com.hospital.facturacion.data.facturador

import java.security.MessageDigest
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

fun interface VerificadorClave {
    fun verifica(clave: String, hashGuardado: String): Boolean
}

/** Hash con formato `pbkdf2-sha256:<iteraciones>:<sal base64>:<hash base64>`. */
class ClavePbkdf2 : VerificadorClave {
    override fun verifica(clave: String, hashGuardado: String): Boolean {
        val partes = hashGuardado.split(':')
        if (partes.size != 4 || partes[0] != "pbkdf2-sha256") return false
        val iteraciones = partes[1].toIntOrNull() ?: return false
        val sal = Base64.getDecoder().decode(partes[2])
        val esperado = Base64.getDecoder().decode(partes[3])
        val especificacion = PBEKeySpec(clave.toCharArray(), sal, iteraciones, esperado.size * 8)
        val calculado = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(especificacion).encoded
        return MessageDigest.isEqual(calculado, esperado)
    }
}
