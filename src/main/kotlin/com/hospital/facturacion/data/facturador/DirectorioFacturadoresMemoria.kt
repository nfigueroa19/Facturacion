package com.hospital.facturacion.data.facturador

import com.hospital.facturacion.data.configuracion.leerArchivo
import com.hospital.facturacion.domain.facturador.DirectorioFacturadores
import com.hospital.facturacion.domain.facturador.Facturador
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.nio.file.Path

/** Forma de cada usuario en `config/facturadores-prueba.json`. */
@Serializable
data class FacturadorPruebaJson(val usuario: String, val nombre: String, val hashClave: String)

/** Usuarios de prueba del piloto. Se reemplaza por el directorio de usuarios del hospital. */
class DirectorioFacturadoresMemoria(
    private val usuarios: List<FacturadorPruebaJson>,
    private val verificador: VerificadorClave,
) : DirectorioFacturadores {
    override fun autenticar(usuario: String, clave: String): Facturador? = usuarios
        .firstOrNull { it.usuario == usuario }
        ?.takeIf { verificador.verifica(clave, it.hashClave) }
        ?.let { Facturador(it.usuario, it.nombre) }
}

fun leerFacturadoresPrueba(archivo: Path, json: Json): List<FacturadorPruebaJson> = json.leerArchivo(archivo)
