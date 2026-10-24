package com.hospital.facturacion.arranque

import io.ktor.server.config.ApplicationConfig
import java.nio.file.Path
import java.time.Duration
import java.time.ZoneId

data class ConfiguracionContenido(val archivo: Path, val directorioImagenes: Path, val tamanoMaximoImagenBytes: Int)

data class ConfiguracionSecretos(val almacen: Path?, val requeridos: Set<String>)

data class ConfiguracionBaseDatos(val url: String, val usuario: String, val aliasClave: String, val conexionesMaximas: Int)

enum class DestinoPersistencia { POSTGRES, APPWRITE }

/** `intentosAppwrite` y `esperaInicialAppwrite` controlan los reintentos ante fallas transitorias de Appwrite. */
data class ConfiguracionPersistencia(
    val destino: DestinoPersistencia,
    val baseDatosAppwrite: String,
    val bucketAppwrite: String,
    val intentosAppwrite: Int,
    val esperaInicialAppwrite: Duration,
)

data class ConfiguracionSolicitudes(
    val formulario: Path,
    val citasPrueba: Path,
    val facturadoresPrueba: Path,
    /** Catálogo de escenarios de prueba (`config/usuarios-prueba.json`). Sin él no hay escenarios ni rutas `/api/dev`. */
    val catalogoPruebas: Path?,
    val tamanoMaximoSoporteBytes: Int,
    val duracionSesion: Duration,
    val zonaHoraria: ZoneId,
)

/** La llave pública no es secreta: Wompi la publica en el navegador de cada pagador. */
data class ConfiguracionPagos(val baseWompi: String, val llavePublicaWompi: String)

data class ConfiguracionAplicacion(
    val contenido: ConfiguracionContenido,
    val solicitudes: ConfiguracionSolicitudes,
    val origenesCors: List<String>,
    val secretos: ConfiguracionSecretos,
    val baseDatos: ConfiguracionBaseDatos,
    val persistencia: ConfiguracionPersistencia,
    val pagos: ConfiguracionPagos,
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
            catalogoPruebas = valor("solicitudes.catalogoPruebas").ifBlank { null }?.let(Path::of),
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
            intentosAppwrite = valor("persistencia.intentosAppwrite").toInt(),
            esperaInicialAppwrite = Duration.ofMillis(valor("persistencia.esperaInicialAppwriteMs").toLong()),
        ),
        pagos = ConfiguracionPagos(baseWompi = valor("pagos.baseWompi"), llavePublicaWompi = valor("pagos.llavePublicaWompi")),
    )
}

private fun String.separadoPorComas(): List<String> = split(',').map(String::trim).filter(String::isNotEmpty)
