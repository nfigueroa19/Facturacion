package com.hospital.facturacion.arranque

import com.hospital.facturacion.AppwriteDePrueba
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals

/** Con `soportes.destino = appwrite` los PDF van al bucket y no a la tabla `soporte`. */
class SoportesAppwriteTest {
    @TempDir
    lateinit var temporal: Path

    @Test
    fun `el paciente radica con los PDF en Appwrite y el facturador los descarga`() = AppwriteDePrueba().use { appwrite ->
        val configuracion = mapOf("prefactura.soportes.destino" to "appwrite")
        probarModulo(temporal, configuracionExtra = configuracion, secretosExtra = appwrite.secretos()) {
            val consecutivo = client.radicarSolicitudCompleta(client.tokenPaciente())
            assertEquals(2, appwrite.archivos.size)
            val ruta = "/api/facturador/solicitudes/$consecutivo/soportes/ORDEN_MEDICA"
            assertEquals("%PDF-1.7 orden", client.get(ruta) { bearerAuth(client.tokenFacturador()) }.bodyAsText())
        }
    }
}
