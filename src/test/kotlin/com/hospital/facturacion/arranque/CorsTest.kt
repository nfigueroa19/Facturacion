package com.hospital.facturacion.arranque

import io.ktor.client.request.header
import io.ktor.client.request.options
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals

/** El frontend publicado en Cloudflare llama al API en Render desde otro origen: la verificación previa debe pasar. */
class CorsTest {
    @TempDir
    lateinit var temporal: Path

    private val origenPoc = "https://facturacion.hosusana.com"

    @Test
    fun `acepta la carga de PDF con token desde el frontend publicado y rechaza otros origenes`() =
        probarModulo(temporal, configuracionExtra = mapOf("prefactura.cors.origenes" to origenPoc)) {
            suspend fun verificacionPrevia(origen: String) = client.options("/api/paciente/soportes/AUTORIZACION") {
                header(HttpHeaders.Origin, origen)
                header(HttpHeaders.AccessControlRequestMethod, "PUT")
                header(HttpHeaders.AccessControlRequestHeaders, "authorization,content-type")
            }
            val aceptada = verificacionPrevia(origenPoc)
            assertEquals(HttpStatusCode.OK, aceptada.status)
            assertEquals(origenPoc, aceptada.headers[HttpHeaders.AccessControlAllowOrigin])
            assertEquals(HttpStatusCode.Forbidden, verificacionPrevia("https://otro.hosusana.com").status)
            assertEquals(HttpStatusCode.Forbidden, verificacionPrevia("http://facturacion.hosusana.com").status)
        }
}
