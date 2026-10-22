package com.hospital.facturacion

import java.nio.file.Files
import java.nio.file.Path
import java.security.KeyStore
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Crea un PKCS12 igual al que produce `keytool -importpass`. Solo valores ficticios. */
object AlmacenesDePrueba {
    fun crear(ruta: Path, clave: CharArray, secretos: Map<String, String>): Path {
        val almacen = KeyStore.getInstance("PKCS12").apply { load(null, clave) }
        val fabrica = SecretKeyFactory.getInstance("PBE")
        secretos.forEach { (alias, valor) ->
            val llave = fabrica.generateSecret(PBEKeySpec(valor.toCharArray()))
            almacen.setEntry(alias, KeyStore.SecretKeyEntry(llave), KeyStore.PasswordProtection(clave))
        }
        Files.newOutputStream(ruta).use { almacen.store(it, clave) }
        return ruta
    }
}
