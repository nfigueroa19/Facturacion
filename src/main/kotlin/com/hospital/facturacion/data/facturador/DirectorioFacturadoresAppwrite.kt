package com.hospital.facturacion.data.facturador

import com.hospital.facturacion.data.appwrite.TablaAppwrite
import com.hospital.facturacion.data.appwrite.idAppwrite
import com.hospital.facturacion.domain.facturador.DirectorioFacturadores
import com.hospital.facturacion.domain.facturador.Facturador
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Fila de `usuarios`. La clave va en formato pbkdf2-sha256:<iteraciones>:<sal>:<hash>; nunca en texto plano. */
@Serializable
data class FilaUsuario(val usuario: String, val nombre: String, @SerialName("hash_clave") val hashClave: String)

class DirectorioFacturadoresAppwrite(
    private val usuarios: TablaAppwrite<FilaUsuario>,
    private val verificador: VerificadorClave,
) : DirectorioFacturadores {
    override fun autenticar(usuario: String, clave: String): Facturador? = usuarios.leer(idAppwrite(usuario))
        ?.takeIf { verificador.verifica(clave, it.hashClave) }
        ?.let { Facturador(it.usuario, it.nombre) }
}

/** Crea o actualiza los usuarios del archivo de arranque. Los que ya no están en el archivo no se borran. */
fun sembrarFacturadores(usuarios: TablaAppwrite<FilaUsuario>, lista: List<FacturadorPruebaJson>) = lista.forEach {
    usuarios.guardar(idAppwrite(it.usuario), FilaUsuario(it.usuario, it.nombre, it.hashClave))
}
