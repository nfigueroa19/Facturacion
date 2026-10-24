package com.hospital.facturacion.api.paciente

import com.hospital.facturacion.arranque.ALIAS_WOMPI_INTEGRIDAD
import com.hospital.facturacion.arranque.ALIAS_WOMPI_PRIVADA
import com.hospital.facturacion.arranque.ALIAS_WOMPI_PUBLICA
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Pasarela simulada y checkout de Wompi: el rechazo se puede reintentar; después del pago el paciente radica. */
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
        assertEquals(HttpStatusCode.Conflict, client.enviar("/api/paciente/copago/checkout", "", token).status)
    }

    @Test
    fun `sin llaves de Wompi el checkout pide la pasarela simulada`() = probarModulo(temporal) {
        val token = client.tokenPaciente()
        assertEquals("SIMULADA", client.enviar("/api/paciente/copago/checkout", "", token).campo("pasarela"))
    }

    @Test
    fun `con las llaves de Wompi el checkout entrega la llave publica, el monto en centavos y la firma`() =
        probarModulo(temporal, secretosExtra = llavesWompi) {
            val respuesta = client.enviar("/api/paciente/copago/checkout", "", client.tokenPaciente())
            assertEquals("CHECKOUT", respuesta.campo("pasarela"))
            val checkout = Json.parseToJsonElement(respuesta.bodyAsText()).jsonObject.getValue("checkout").jsonObject
            assertEquals("pub_test_prueba", checkout.getValue("llavePublica").jsonPrimitive.content)
            assertEquals("500000", checkout.getValue("montoCentavos").jsonPrimitive.content)
            assertEquals(64, checkout.getValue("firma").jsonPrimitive.content.length)
            assertTrue("test_integrity" !in respuesta.bodyAsText())
        }
}

private val llavesWompi = mapOf(
    ALIAS_WOMPI_PUBLICA to "pub_test_prueba",
    ALIAS_WOMPI_PRIVADA to "prv_test_prueba",
    ALIAS_WOMPI_INTEGRIDAD to "test_integrity_prueba",
)
