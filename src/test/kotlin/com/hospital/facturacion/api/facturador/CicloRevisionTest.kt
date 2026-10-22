package com.hospital.facturacion.api.facturador

import com.hospital.facturacion.arranque.campo
import com.hospital.facturacion.arranque.datosCompletos
import com.hospital.facturacion.arranque.enviar
import com.hospital.facturacion.arranque.probarModulo
import com.hospital.facturacion.arranque.radicarSolicitudCompleta
import com.hospital.facturacion.arranque.tokenFacturador
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

/** Novedad, corrección del paciente, aprobación con checklist y llegada confirmada. */
class CicloRevisionTest {
    @TempDir
    lateinit var soportes: Path

    @Test
    fun `una solicitud con novedades vuelve a revision y termina lista para facturar`() = probarModulo(soportes) {
        val paciente = client.tokenPaciente()
        val ruta = "/api/facturador/solicitudes/" + client.radicarSolicitudCompleta(paciente)
        val facturador = client.tokenFacturador()
        assertEquals(HttpStatusCode.NoContent, client.enviar("$ruta/novedades", """{"descripcion":"Falta firma en la orden"}""", facturador).status)
        val prefactura = client.get("/api/paciente/prefactura") { bearerAuth(paciente) }.bodyAsText()
        assertTrue(prefactura.contains("PENDIENTE") && prefactura.contains("Falta firma en la orden"))
        assertEquals(HttpStatusCode.OK, client.enviar("/api/paciente/solicitud", datosCompletos, paciente).status)
        val incompleto = client.enviar("$ruta/aprobar", """{"revisados":["DATOS","AUTORIZACION","ORDEN_MEDICA"]}""", facturador)
        assertEquals("checklist" to HttpStatusCode.UnprocessableEntity, incompleto.campo("error") to incompleto.status)
        val todos = """{"revisados":["DATOS","AUTORIZACION","ORDEN_MEDICA","COPAGO"]}"""
        assertEquals(HttpStatusCode.NoContent, client.enviar("$ruta/aprobar", todos, facturador).status)
        assertEquals(HttpStatusCode.Conflict, client.enviar("$ruta/aprobar", todos, facturador).status)
        assertEquals(HttpStatusCode.NoContent, client.enviar("$ruta/llegada", "", facturador).status)
        val detalle = client.get(ruta) { bearerAuth(facturador) }.bodyAsText()
        assertTrue(detalle.contains("LISTO_PARA_FACTURAR") && detalle.contains(""""facturador":"facturador""""))
    }
}
