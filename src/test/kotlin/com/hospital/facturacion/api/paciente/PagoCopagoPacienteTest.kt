package com.hospital.facturacion.api.paciente

import com.hospital.facturacion.COMERCIO_WOMPI
import com.hospital.facturacion.arranque.campo
import com.hospital.facturacion.arranque.enviar
import com.hospital.facturacion.arranque.radicarSolicitudCompleta
import com.hospital.facturacion.arranque.subirPdf
import com.hospital.facturacion.arranque.tokenPaciente
import com.hospital.facturacion.transaccionWompi
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** El rechazo de Wompi se puede reintentar; después del pago el paciente radica. */
class PagoCopagoPacienteTest {
    @TempDir
    lateinit var temporal: Path

    @Test
    fun `reintenta la transaccion rechazada, paga, radica y no admite cambios mientras esta en revision`() = conWompi(temporal) { wompi ->
        wompi.responder("/v1/merchants/info", 200, COMERCIO_WOMPI)
        val token = client.tokenPaciente()
        val referencia = client.enviar("/api/paciente/copago/checkout", "", token).campo("referencia")
        wompi.responder("/v1/transactions/tx-1", 200, transaccionWompi("tx-1", "DECLINED", referencia))
        wompi.responder("/v1/transactions/tx-2", 200, transaccionWompi("tx-2", "APPROVED", referencia))
        val rechazado = client.enviar("/api/paciente/copago", """{"token":"tx-1"}""", token)
        assertEquals("RECHAZADA", rechazado.bodyAsText().substringAfter("[\"").substringBefore("\""))
        assertEquals(HttpStatusCode.UnprocessableEntity, client.enviar("/api/paciente/copago", """{"token":"4242 4242"}""", token).status)
        assertEquals("tx-2", client.enviar("/api/paciente/copago", """{"token":"tx-2"}""", token).campo("referencia"))
        assertEquals("PF-000001", client.radicarSolicitudCompleta(token))
        val prefactura = client.get("/api/paciente/prefactura") { bearerAuth(token) }
        assertTrue(prefactura.bodyAsText().contains(""""estado":"EN_REVISION""""))
        assertEquals("no-store", prefactura.headers["Cache-Control"])
        assertEquals(HttpStatusCode.Conflict, client.subirPdf("AUTORIZACION", "%PDF-1.7", token).status)
        assertEquals(HttpStatusCode.Conflict, client.enviar("/api/paciente/copago/checkout", "", token).status)
    }
}
