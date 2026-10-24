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

/** Pasarela de pruebas: el rechazo se puede reintentar; después del pago el paciente radica. */
class CopagoPacienteTest {
    @TempDir
    lateinit var temporal: Path

    @Test
    fun `reintenta el copago rechazado por la pasarela de pruebas, paga, radica y no admite cambios mientras esta en revision`() = probarModulo(temporal) {
        val token = client.tokenPaciente()
        val rechazado = client.enviar("/api/paciente/copago", """{"token":"tok_prueba_fondos_insuficientes"}""", token)
        assertEquals("FONDOS_INSUFICIENTES", rechazado.bodyAsText().substringAfter("[\"").substringBefore("\""))
        assertEquals(HttpStatusCode.UnprocessableEntity, client.enviar("/api/paciente/copago", """{"token":"4242 4242"}""", token).status)
        assertTrue(client.enviar("/api/paciente/copago", """{"token":"tok_prueba_aprobada"}""", token).campo("referencia").startsWith("SIM-"))
        val faltan = client.enviar("/api/paciente/solicitud", """{"datos":{}}""", token)
        assertEquals(HttpStatusCode.UnprocessableEntity, faltan.status)
        assertEquals("PF-000001", client.radicarSolicitudCompleta(token))
        val prefactura = client.get("/api/paciente/prefactura") { bearerAuth(token) }
        assertTrue(prefactura.bodyAsText().contains(""""estado":"EN_REVISION""""))
        assertEquals("no-store", prefactura.headers["Cache-Control"])
        assertEquals(HttpStatusCode.Conflict, client.subirPdf("AUTORIZACION", "%PDF-1.7", token).status)
    }
}
