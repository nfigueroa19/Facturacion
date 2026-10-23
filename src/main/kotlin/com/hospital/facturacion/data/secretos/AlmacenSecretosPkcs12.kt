package com.hospital.facturacion.data.secretos

import java.nio.file.Files
import java.nio.file.Path
import java.security.KeyStore
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Almacén PKCS12 cifrado creado con `keytool -importpass`. `load` verifica la integridad del archivo
 * y falla si la clave es incorrecta o el archivo fue alterado.
 */
class AlmacenSecretosPkcs12(ruta: Path, private val clave: CharArray) : FuenteSecretos {
    private val almacen: KeyStore = KeyStore.getInstance("PKCS12").apply {
        Files.newInputStream(ruta).use { load(it, clave) }
    }

    override fun aliases(): Set<String> = almacen.aliases().toList().toSet()

    override fun leer(alias: String): Secreto? {
        val entrada = almacen.getEntry(alias, KeyStore.PasswordProtection(clave)) as? KeyStore.SecretKeyEntry
            ?: return null
        val llave = entrada.secretKey
        val especificacion = SecretKeyFactory.getInstance(llave.algorithm)
            .getKeySpec(llave, PBEKeySpec::class.java) as? PBEKeySpec ?: return null
        return Secreto(especificacion.password).also { especificacion.clearPassword() }
    }
}
