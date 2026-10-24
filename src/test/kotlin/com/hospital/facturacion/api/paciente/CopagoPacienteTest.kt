package com.hospital.facturacion.api.paciente

import com.hospital.facturacion.COMERCIO_WOMPI
import com.hospital.facturacion.WompiDePrueba
import com.hospital.facturacion.arranque.campo
import com.hospital.facturacion.arranque.enviar
import com.hospital.facturacion.arranque.probarModulo
import com.hospital.facturacion.arranque.tokenPaciente
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.ApplicationTestBuilder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals

/** Módulo completo con la base de Wompi apuntando a una imitación local. */
internal fun conWompi(temporal: Path, bloque: suspend ApplicationTestBuilder.(WompiDePrueba) -> Unit) = WompiDePrueba().use { wompi ->
    probarModulo(temporal, configuracionExtra = mapOf("prefactura.pagos.baseWompi" to wompi.base)) { bloque(wompi) }
}

/** Checkout del copago contra una imitación local de Wompi. */
class CopagoPacienteTest {
    @TempDir
    lateinit var temporal: Path

    @Test
    fun `entrega la llave publica, el monto y los contratos de Wompi sin firma de integridad`() = conWompi(temporal) { wompi ->
        wompi.responder("/v1/merchants/info", 200, COMERCIO_WOMPI)
        val checkout = Json.parseToJsonElement(client.enviar("/api/paciente/copago/checkout", "", client.tokenPaciente()).bodyAsText()).jsonObject
        assertEquals("pub_test_prueba", checkout.getValue("llavePublica").jsonPrimitive.content)
        assertEquals("500000", checkout.getValue("montoCentavos").jsonPrimitive.content)
        assertEquals(JsonNull, checkout.getValue("firma"))
        val terminos = checkout.getValue("aceptacion").jsonObject.getValue("terminos").jsonObject
        assertEquals("tok-terminos", terminos.getValue("token").jsonPrimitive.content)
        assertEquals("https://wompi.test/reglamento.pdf", terminos.getValue("enlace").jsonPrimitive.content)
    }

    @Test
    fun `sin respuesta de Wompi el checkout no esta disponible`() = conWompi(temporal) {
        val respuesta = client.enviar("/api/paciente/copago/checkout", "", client.tokenPaciente())
        assertEquals(HttpStatusCode.ServiceUnavailable, respuesta.status)
        assertEquals("pasarela-no-disponible", respuesta.campo("error"))
    }
}
