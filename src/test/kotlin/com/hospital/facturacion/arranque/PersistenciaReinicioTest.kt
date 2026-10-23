package com.hospital.facturacion.arranque

import com.hospital.facturacion.PostgresDePrueba
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Lo que radica el paciente sigue ahí cuando el servidor arranca de nuevo contra la misma base. */
class PersistenciaReinicioTest {
    @TempDir
    lateinit var temporal: Path

    @Test
    fun `la solicitud, sus soportes y el pago sobreviven al reinicio del servidor`() {
        val base = PostgresDePrueba.baseNueva()
        var consecutivo = ""
        probarModulo(temporal, base) { consecutivo = client.radicarSolicitudCompleta(client.tokenPaciente()) }
        probarModulo(temporal, base) {
            val facturador = client.tokenFacturador()
            val ruta = "/api/facturador/solicitudes/$consecutivo"
            assertTrue(client.get(ruta) { bearerAuth(facturador) }.bodyAsText().contains("EN_REVISION"))
            assertEquals(HttpStatusCode.OK, client.get("$ruta/soportes/ORDEN_MEDICA") { bearerAuth(facturador) }.status)
            assertTrue(client.get("/api/paciente/prefactura") { bearerAuth(client.tokenPaciente()) }.bodyAsText().contains(consecutivo))
        }
    }
}
