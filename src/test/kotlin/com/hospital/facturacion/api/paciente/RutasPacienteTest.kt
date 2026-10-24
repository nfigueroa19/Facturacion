package com.hospital.facturacion.api.paciente

import com.hospital.facturacion.arranque.campo
import com.hospital.facturacion.arranque.enviar
import com.hospital.facturacion.arranque.probarModulo
import com.hospital.facturacion.arranque.radicarSolicitudCompleta
import com.hospital.facturacion.arranque.subirPdf
import com.hospital.facturacion.arranque.tokenPaciente
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RutasPacienteTest {
    @TempDir
    lateinit var temporal: Path

    @Test
    fun `rechaza credenciales erradas y rutas sin sesion`() = probarModulo(temporal) {
        val errado = client.enviar("/api/paciente/sesion", """{"identificacion":"1000000001","codigoAcceso":"OTRO"}""")
        assertEquals(HttpStatusCode.Unauthorized, errado.status)
        assertEquals(HttpStatusCode.Unauthorized, client.get("/api/paciente/prefactura").status)
        assertEquals(HttpStatusCode.Unauthorized, client.get("/api/paciente/prefactura") { bearerAuth("inventado") }.status)
        assertEquals(HttpStatusCode.BadRequest, client.enviar("/api/paciente/sesion", "{no es json").status)
    }

    @Test
    fun `valida los PDF por contenido y por tamano`() = probarModulo(temporal) {
        val token = client.tokenPaciente()
        assertEquals("NO_ES_PDF", client.subirPdf("AUTORIZACION", "\u0089PNG imagen", token).campo("error"))
        assertEquals("EXCEDE_TAMANO", client.subirPdf("AUTORIZACION", "%PDF-" + "x".repeat(1024), token).campo("error"))
        assertEquals(HttpStatusCode.NotFound, client.subirPdf("FOTO", "%PDF-1.7", token).status)
        assertEquals(HttpStatusCode.NoContent, client.subirPdf("AUTORIZACION", "%PDF-1.7", token).status)
    }
}
