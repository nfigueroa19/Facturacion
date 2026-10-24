package com.hospital.facturacion.data.facturador

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Hash en el formato que lee [ClavePbkdf2], con los mismos parámetros de `despliegue/GenerarHashClave.java`. */
class GeneradorHashPbkdf2(private val aleatorio: SecureRandom) {
    fun hash(clave: String): String {
        val sal = ByteArray(16).also(aleatorio::nextBytes)
        val especificacion = PBEKeySpec(clave.toCharArray(), sal, ITERACIONES, 256)
        val hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(especificacion).encoded
        especificacion.clearPassword()
        val base64 = Base64.getEncoder()
        return "pbkdf2-sha256:$ITERACIONES:${base64.encodeToString(sal)}:${base64.encodeToString(hash)}"
    }

    private companion object {
        const val ITERACIONES = 210_000
    }
}
