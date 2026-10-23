package com.hospital.facturacion.api.facturador

import com.hospital.facturacion.arranque.campo
import com.hospital.facturacion.arranque.enviar
import com.hospital.facturacion.arranque.probarModulo
import com.hospital.facturacion.arranque.radicarSolicitudCompleta
import com.hospital.facturacion.arranque.tokenFacturador
import com.hospital.facturacion.arranque.tokenPaciente
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RutasFacturadorTest {
    @TempDir
    lateinit var temporal: Path

    @Test
    fun `el panel exige la sesion del facturador, no la del paciente`() = probarModulo(temporal) {
        assertEquals(HttpStatusCode.Unauthorized, client.enviar("/api/facturador/sesion", """{"usuario":"facturador","clave":"x"}""").status)
        val paciente = client.tokenPaciente()
        assertEquals(HttpStatusCode.Unauthorized, client.get("/api/facturador/solicitudes") { bearerAuth(paciente) }.status)
        assertEquals("Facturador de prueba", client.enviar("/api/facturador/sesion", """{"usuario":"facturador","clave":"prueba123"}""").campo("nombre"))
    }

    @Test
    fun `revisa la cola, abre los PDF y muestra los datos con su etiqueta`() = probarModulo(temporal) {
        val consecutivo = client.radicarSolicitudCompleta(client.tokenPaciente())
        val token = client.tokenFacturador()
        val cola = client.get("/api/facturador/solicitudes?estado=EN_REVISION") { bearerAuth(token) }.bodyAsText()
        assertTrue(cola.contains(consecutivo) && cola.contains("1000000001"))
        assertEquals("[]", client.get("/api/facturador/solicitudes?estado=APROBADO") { bearerAuth(token) }.bodyAsText())
        assertEquals(HttpStatusCode.BadRequest, client.get("/api/facturador/solicitudes?estado=RECHAZADO") { bearerAuth(token) }.status)
        val detalle = client.get("/api/facturador/solicitudes/$consecutivo") { bearerAuth(token) }.bodyAsText()
        assertTrue(detalle.contains(""""etiqueta":"Nombre del padre","valor":"Padre de prueba""""))
        val pdf = client.get("/api/facturador/solicitudes/$consecutivo/soportes/ORDEN_MEDICA") { bearerAuth(token) }
        assertEquals(ContentType.Application.Pdf, pdf.contentType())
        assertEquals("%PDF-1.7 orden", pdf.bodyAsText())
    }
}
