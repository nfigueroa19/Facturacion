package com.hospital.facturacion.arranque

import com.hospital.facturacion.AlmacenesDePrueba
import com.hospital.facturacion.BaseDePrueba
import com.hospital.facturacion.PostgresDePrueba
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.nio.file.Path

/**
 * Módulo completo contra los archivos reales de `config/` y `assets/` y una base PostgreSQL embebida.
 * La clave de la base sale de un almacén PKCS12 creado en `temporal`, igual que en producción.
 */
fun probarModulo(
    temporal: Path,
    base: BaseDePrueba = PostgresDePrueba.baseNueva(),
    bloque: suspend ApplicationTestBuilder.() -> Unit,
) = testApplication {
    val almacen = AlmacenesDePrueba.crear(temporal.resolve("secretos.p12"), CLAVE_ALMACEN.toCharArray(), mapOf("basedatos.clave" to "sin-uso"))
    environment {
        config = MapApplicationConfig(
            "prefactura.contenido.archivo" to "config/contenido.json",
            "prefactura.contenido.imagenes" to "assets/imagenes",
            "prefactura.contenido.tamanoMaximoImagenBytes" to "2097152",
            "prefactura.solicitudes.formulario" to "config/formulario-prefactura.json",
            "prefactura.solicitudes.citasPrueba" to "config/citas-prueba.json",
            "prefactura.solicitudes.facturadoresPrueba" to "config/facturadores-prueba.json",
            "prefactura.solicitudes.tamanoMaximoSoporteBytes" to "1024",
            "prefactura.solicitudes.minutosSesion" to "30",
            "prefactura.solicitudes.zonaHoraria" to "America/Bogota",
            "prefactura.cors.origenes" to "http://localhost:5173",
            "prefactura.secretos.almacen" to almacen.toString(),
            "prefactura.secretos.requeridos" to "basedatos.clave",
            "prefactura.basedatos.host" to "localhost",
            "prefactura.basedatos.puerto" to base.puerto.toString(),
            "prefactura.basedatos.nombre" to base.nombre,
            "prefactura.basedatos.usuario" to "postgres",
            "prefactura.basedatos.ssl" to "disable",
            "prefactura.basedatos.aliasClave" to "basedatos.clave",
            "prefactura.basedatos.conexionesMaximas" to "2",
        )
    }
    application { moduloCon(mapOf(VARIABLE_CLAVE to CLAVE_ALMACEN)::get) }
    bloque()
}

// El PostgreSQL embebido acepta al usuario postgres sin clave; el valor del alias no se valida aquí.
private const val CLAVE_ALMACEN = "clave-almacen-prueba"

suspend fun HttpClient.enviar(ruta: String, json: String, token: String? = null): HttpResponse = post(ruta) {
    contentType(ContentType.Application.Json)
    token?.let { bearerAuth(it) }
    setBody(json)
}

suspend fun HttpClient.subirPdf(tipo: String, contenido: String, token: String): HttpResponse =
    put("/api/paciente/soportes/$tipo") {
        bearerAuth(token)
        contentType(ContentType.Application.Pdf)
        setBody(contenido.toByteArray())
    }

suspend fun HttpResponse.campo(nombre: String): String =
    requireNotNull(Json.parseToJsonElement(bodyAsText()).jsonObject[nombre]) { "Falta '$nombre'" }.jsonPrimitive.content

suspend fun HttpClient.tokenPaciente(): String =
    enviar("/api/paciente/sesion", """{"identificacion":"1000000001","codigoAcceso":"PRUEBA1"}""").campo("token")

suspend fun HttpClient.tokenFacturador(): String =
    enviar("/api/facturador/sesion", """{"usuario":"facturador","clave":"prueba123"}""").campo("token")

val datosCompletos = """
    {"datos":{"nombres-apellidos":"Paciente de prueba","fecha-nacimiento":"1990-01-01","nombre-padre":"Padre de prueba",
    "nombre-madre":"Madre de prueba","eps":"EPS de prueba","celular":"3000000000","direccion":"Calle de prueba",
    "municipio":"Municipio de prueba"}}
""".trimIndent()

/** Carga los dos soportes y radica; devuelve el consecutivo. */
suspend fun HttpClient.radicarSolicitudCompleta(token: String): String {
    subirPdf("AUTORIZACION", "%PDF-1.7 autorizacion", token)
    subirPdf("ORDEN_MEDICA", "%PDF-1.7 orden", token)
    return enviar("/api/paciente/solicitud", datosCompletos, token).campo("consecutivo")
}
