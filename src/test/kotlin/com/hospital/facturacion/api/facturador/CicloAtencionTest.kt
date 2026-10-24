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
import io.ktor.http.HttpStatusCode
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Día de la cita: ingreso, llegada por el digiturno, error de Dinámica, reintento y factura. La cita de prueba 1 es hoy. */
class CicloAtencionTest {
    @TempDir
    lateinit var temporal: Path

    @Test
    fun `una solicitud aprobada termina facturada despues de un intento fallido`() = probarModulo(temporal) {
        val ruta = "/api/facturador/solicitudes/" + client.radicarSolicitudCompleta(client.tokenPaciente())
        val facturador = client.tokenFacturador()
        client.enviar("$ruta/aprobar", """{"revisados":["DATOS","AUTORIZACION","ORDEN_MEDICA","COPAGO"]}""", facturador)
        val antesDeLaLlegada = client.enviar("$ruta/factura", """{"numero":"FE-1"}""", facturador)
        assertEquals(HttpStatusCode.Conflict to "transicion", antesDeLaLlegada.status to antesDeLaLlegada.campo("error"))
        assertEquals(HttpStatusCode.UnprocessableEntity, client.enviar("$ruta/ingreso", """{"numero":"ING 1"}""", facturador).status)
        assertEquals(HttpStatusCode.NoContent, client.enviar("$ruta/ingreso", """{"numero":"ING-1"}""", facturador).status)
        val turno = client.enviar("/api/digiturno/llegadas", """{"identificacion":"1000000001"}""", facturador)
        assertEquals("PRECONSULTA" to "LISTO_PARA_FACTURAR", turno.campo("destino") to turno.campo("estado"))
        assertEquals(HttpStatusCode.NoContent, client.enviar("$ruta/intentos-facturacion", """{"texto":"Contrato no encontrado"}""", facturador).status)
        assertEquals(HttpStatusCode.NoContent, client.enviar("$ruta/factura", """{"numero":"FE-1"}""", facturador).status)
        val detalle = client.get(ruta) { bearerAuth(facturador) }.bodyAsText()
        assertTrue(detalle.contains(""""estado":"FACTURADO"""") && detalle.contains("Contrato no encontrado") && detalle.contains("FE-1"))
        assertEquals(HttpStatusCode.Conflict, client.enviar("$ruta/anulacion", """{"texto":"No llegó"}""", facturador).status)
    }
}
