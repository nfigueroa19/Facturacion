package com.hospital.facturacion.arranque

import io.ktor.server.config.ApplicationConfig
import java.nio.file.Path

data class ConfiguracionContenido(val archivo: Path, val directorioImagenes: Path, val tamanoMaximoImagenBytes: Int)

data class ConfiguracionSecretos(val almacen: Path?, val requeridos: Set<String>)

data class ConfiguracionAplicacion(
    val contenido: ConfiguracionContenido,
    val origenesCors: List<String>,
    val secretos: ConfiguracionSecretos,
)

fun ApplicationConfig.configuracionAplicacion(): ConfiguracionAplicacion {
    fun valor(clave: String) = property("prefactura.$clave").getString()
    return ConfiguracionAplicacion(
        contenido = ConfiguracionContenido(
            archivo = Path.of(valor("contenido.archivo")),
            directorioImagenes = Path.of(valor("contenido.imagenes")),
            tamanoMaximoImagenBytes = valor("contenido.tamanoMaximoImagenBytes").toInt(),
        ),
        origenesCors = valor("cors.origenes").separadoPorComas(),
        secretos = ConfiguracionSecretos(
            almacen = valor("secretos.almacen").ifBlank { null }?.let(Path::of),
            requeridos = valor("secretos.requeridos").separadoPorComas().toSet(),
        ),
    )
}

private fun String.separadoPorComas(): List<String> = split(',').map(String::trim).filter(String::isNotEmpty)
