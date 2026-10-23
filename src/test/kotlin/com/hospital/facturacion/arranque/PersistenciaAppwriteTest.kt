package com.hospital.facturacion.arranque

import com.hospital.facturacion.AppwriteDePrueba
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Con `persistencia.destino = appwrite` las tablas y los PDF van a Appwrite y sobreviven al reinicio del servidor. */
class PersistenciaAppwriteTest {
    @TempDir
    lateinit var temporal: Path

    @Test
    fun `el paciente radica contra Appwrite y tras el reinicio el facturador ve la solicitud y sus soportes`() = AppwriteDePrueba().use { appwrite ->
        val configuracion = mapOf("prefactura.persistencia.destino" to "appwrite")
        var consecutivo = ""
        probarModulo(temporal, configuracionExtra = configuracion, secretosExtra = appwrite.secretos()) {
            consecutivo = client.radicarSolicitudCompleta(client.tokenPaciente())
        }
        assertEquals(2, appwrite.archivos.size)
        assertEquals(1, appwrite.tablas.de("solicitudes").size)
        probarModulo(temporal, configuracionExtra = configuracion, secretosExtra = appwrite.secretos()) {
            val facturador = client.tokenFacturador()
            assertTrue(client.get("/api/facturador/solicitudes/$consecutivo") { bearerAuth(facturador) }.bodyAsText().contains("EN_REVISION"))
            val ruta = "/api/facturador/solicitudes/$consecutivo/soportes/ORDEN_MEDICA"
            assertEquals("%PDF-1.7 orden", client.get(ruta) { bearerAuth(facturador) }.bodyAsText())
        }
    }
}
