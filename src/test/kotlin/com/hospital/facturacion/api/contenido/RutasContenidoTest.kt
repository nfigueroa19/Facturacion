package com.hospital.facturacion.api.contenido

import com.hospital.facturacion.ImagenesDePrueba
import com.hospital.facturacion.api.plataforma.instalarPlataforma
import com.hospital.facturacion.domain.contenido.ClaveRecurso
import com.hospital.facturacion.domain.contenido.ContenidoInstitucional
import com.hospital.facturacion.domain.contenido.FuenteContenido
import com.hospital.facturacion.domain.contenido.NombreArchivoImagen
import com.hospital.facturacion.service.contenido.ConsultarTextosInstitucionales
import com.hospital.facturacion.service.contenido.ObtenerImagenInstitucional
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class RutasContenidoTest {
    private val clave = requireNotNull(ClaveRecurso.de("logo-encabezado"))
    private val fuente = FuenteContenido {
        ContenidoInstitucional(mapOf("titulo" to "Prefactura"), mapOf(clave to requireNotNull(NombreArchivoImagen.de("logo.png"))))
    }

    private fun probar(bloque: suspend ApplicationTestBuilder.() -> Unit) = testApplication {
        application {
            instalarPlataforma(listOf("http://localhost:5173"))
            routing { rutasContenido(ConsultarTextosInstitucionales(fuente) { it }, ObtenerImagenInstitucional(fuente, { ImagenesDePrueba.png() }, { it })) }
        }
        bloque()
    }

    @Test
    fun `entrega textos en JSON e imagenes con su tipo y cabeceras de seguridad`() = probar {
        assertEquals("""{"titulo":"Prefactura"}""", client.get("/api/contenido/textos").bodyAsText())
        val imagen = client.get("/api/contenido/imagenes/logo-encabezado")
        assertEquals(ContentType.Image.PNG, imagen.contentType())
        assertEquals("nosniff", imagen.headers["X-Content-Type-Options"])
    }

    @Test
    fun `responde 404 a claves desconocidas e intentos de path traversal`() = probar {
        listOf("otra", "LOGO-ENCABEZADO", "..%2F..%2Fbuild.gradle.kts", "%2e%2e%2fconfig%2fcontenido.json", "..%5Csecreto", "logo-encabezado%00")
            .forEach { assertEquals(HttpStatusCode.NotFound, client.get("/api/contenido/imagenes/$it").status, it) }
    }
}
