package com.hospital.facturacion.arranque

import com.hospital.facturacion.contiene
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/** Arranca el módulo completo contra `config/` y `assets/imagenes/` reales del proyecto. */
class ModuloTest {
    private val configuracion = MapApplicationConfig(
        "prefactura.contenido.archivo" to "config/contenido.json",
        "prefactura.contenido.imagenes" to "assets/imagenes",
        "prefactura.contenido.tamanoMaximoImagenBytes" to "2097152",
        "prefactura.cors.origenes" to "http://localhost:5173",
        "prefactura.secretos.almacen" to "",
        "prefactura.secretos.requeridos" to "",
    )

    @Test
    fun `sirve salud y los logos institucionales sin metadatos de texto`() = testApplication {
        environment { config = configuracion }
        application { modulo() }
        assertEquals("""{"estado":"activo"}""", client.get("/salud").bodyAsText())
        for (clave in listOf("logo-encabezado", "logo-acreditacion", "logo-pie-pagina")) {
            val respuesta = client.get("/api/contenido/imagenes/$clave")
            assertEquals(HttpStatusCode.OK, respuesta.status, clave)
            val bytes = respuesta.bodyAsBytes()
            listOf("tEXt", "iTXt", "zTXt", "eXIf", "tIME").forEach { assertFalse(bytes.contiene(it), "$clave contiene $it") }
        }
    }
}
