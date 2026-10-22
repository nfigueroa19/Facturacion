package com.hospital.facturacion.arranque

import com.hospital.facturacion.contiene
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ModuloTest {
    @TempDir
    lateinit var soportes: Path

    @Test
    fun `sirve salud y los logos institucionales sin metadatos de texto`() = probarModulo(soportes) {
        assertEquals("""{"estado":"activo"}""", client.get("/salud").bodyAsText())
        for (clave in listOf("logo-encabezado", "logo-acreditacion", "logo-pie-pagina")) {
            val respuesta = client.get("/api/contenido/imagenes/$clave")
            assertEquals(HttpStatusCode.OK, respuesta.status, clave)
            val bytes = respuesta.bodyAsBytes()
            listOf("tEXt", "iTXt", "zTXt", "eXIf", "tIME").forEach { assertFalse(bytes.contiene(it), "$clave contiene $it") }
        }
    }
}
