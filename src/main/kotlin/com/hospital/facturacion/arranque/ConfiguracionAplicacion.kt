package com.hospital.facturacion.arranque

import io.ktor.server.config.ApplicationConfig
import java.nio.file.Path
import java.time.Duration
import java.time.ZoneId

data class ConfiguracionContenido(val archivo: Path, val directorioImagenes: Path, val tamanoMaximoImagenBytes: Int)

data class ConfiguracionSecretos(val almacen: Path?, val requeridos: Set<String>)

data class ConfiguracionBaseDatos(val url: String, val usuario: String, val aliasClave: String, val conexionesMaximas: Int)

enum class DestinoPersistencia { POSTGRES, APPWRITE }

data class ConfiguracionPersistencia(val destino: DestinoPersistencia, val baseDatosAppwrite: String, val bucketAppwrite: String)

data class ConfiguracionSolicitudes(
    val formulario: Path,
    val citasPrueba: Path,
    val facturadoresPrueba: Path,
    val tamanoMaximoSoporteBytes: Int,
    val duracionSesion: Duration,
    val zonaHoraria: ZoneId,
)

data class ConfiguracionAplicacion(
    val contenido: ConfiguracionContenido,
    val solicitudes: ConfiguracionSolicitudes,
    val origenesCors: List<String>,
    val secretos: ConfiguracionSecretos,
    val baseDatos: ConfiguracionBaseDatos,
    val persistencia: ConfiguracionPersistencia,
)

fun ApplicationConfig.configuracionAplicacion(): ConfiguracionAplicacion {
    fun valor(clave: String) = property("prefactura.$clave").getString()
    return ConfiguracionAplicacion(
        contenido = ConfiguracionContenido(
            archivo = Path.of(valor("contenido.archivo")),
            directorioImagenes = Path.of(valor("contenido.imagenes")),
            tamanoMaximoImagenBytes = valor("contenido.tamanoMaximoImagenBytes").toInt(),
        ),
        solicitudes = ConfiguracionSolicitudes(
            formulario = Path.of(valor("solicitudes.formulario")),
            citasPrueba = Path.of(valor("solicitudes.citasPrueba")),
            facturadoresPrueba = Path.of(valor("solicitudes.facturadoresPrueba")),
            tamanoMaximoSoporteBytes = valor("solicitudes.tamanoMaximoSoporteBytes").toInt(),
            duracionSesion = Duration.ofMinutes(valor("solicitudes.minutosSesion").toLong()),
            zonaHoraria = ZoneId.of(valor("solicitudes.zonaHoraria")),
        ),
        origenesCors = valor("cors.origenes").separadoPorComas(),
        secretos = ConfiguracionSecretos(
            almacen = valor("secretos.almacen").ifBlank { null }?.let(Path::of),
            requeridos = valor("secretos.requeridos").separadoPorComas().toSet(),
        ),
        baseDatos = ConfiguracionBaseDatos(
            url = "jdbc:postgresql://${valor("basedatos.host")}:${valor("basedatos.puerto")}/${valor("basedatos.nombre")}" +
                "?sslmode=${valor("basedatos.ssl")}",
            usuario = valor("basedatos.usuario"),
            aliasClave = valor("basedatos.aliasClave"),
            conexionesMaximas = valor("basedatos.conexionesMaximas").toInt(),
        ),
        persistencia = ConfiguracionPersistencia(
            destino = DestinoPersistencia.valueOf(valor("persistencia.destino").uppercase()),
            baseDatosAppwrite = valor("persistencia.baseDatosAppwrite"),
            bucketAppwrite = valor("persistencia.bucketAppwrite"),
        ),
    )
}

private fun String.separadoPorComas(): List<String> = split(',').map(String::trim).filter(String::isNotEmpty)
