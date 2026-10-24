package com.hospital.facturacion.arranque

import com.hospital.facturacion.AppwriteDePrueba
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.ApplicationTestBuilder
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// Catálogo real de `config/usuarios-prueba.json` contra la imitación de Appwrite.

private val conCatalogo = mapOf(
    "prefactura.persistencia.destino" to "appwrite",
    "prefactura.solicitudes.catalogoPruebas" to "config/usuarios-prueba.json",
)

private fun probarConCatalogo(temporal: Path, appwrite: AppwriteDePrueba, bloque: suspend ApplicationTestBuilder.() -> Unit) =
    probarModulo(temporal, configuracionExtra = conCatalogo, secretosExtra = appwrite.secretos(), bloque = bloque)

private suspend fun HttpClient.prefactura(identificacion: String, codigo: String): String {
    val token = enviar("/api/paciente/sesion", """{"identificacion":"$identificacion","codigoAcceso":"$codigo"}""").campo("token")
    return get("/api/paciente/prefactura") { bearerAuth(token) }.bodyAsText()
}

private suspend fun HttpClient.personal(usuario: String) = enviar("/api/facturador/sesion", """{"usuario":"$usuario","clave":"prueba123"}""").campo("token")

class EscenariosPruebaTest {
    @TempDir
    lateinit var temporal: Path

    @Test
    fun `publica el catalogo y siembra cada escenario en su estado con los usuarios del personal`() = AppwriteDePrueba().use { appwrite ->
        probarConCatalogo(temporal, appwrite) {
            assertTrue(client.get("/api/dev/catalogo").bodyAsText().contains("\"codigoAcceso\":\"NOVEDAD\""))
            assertTrue(client.prefactura("3333333333", "NOVEDAD").contains("PENDIENTE"))
            assertTrue(client.prefactura("8888888888", "FACTURADA").contains("FACTURADO"))
            val llegada = client.enviar("/api/digiturno/llegadas", """{"identificacion":"5555555555"}""", client.personal("digiturno"))
            assertEquals("P-001", llegada.campo("turno"))
            assertEquals(HttpStatusCode.Unauthorized, client.enviar("/api/dev/poblar", "{}").status)
        }
    }
}

class RestablecerEscenariosTest {
    @TempDir
    lateinit var temporal: Path

    @Test
    fun `el reinicio conserva lo que hizo el probador y restablecer vuelve a la etapa inicial`() = AppwriteDePrueba().use { appwrite ->
        val hoy = LocalDate.now(ZoneId.of("America/Bogota"))
        probarConCatalogo(temporal, appwrite) {
            client.radicarSolicitudCompleta(client.enviar("/api/paciente/sesion", """{"identificacion":"1111111111","codigoAcceso":"FELIZ"}""").campo("token"))
            val cita = """{"identificacion":"1010101010","celular":"3000000101","fecha":"$hoy","medico":"Médico","especialidad":"General","regimen":"SUBSIDIADO"}"""
            assertEquals(HttpStatusCode.Created, client.enviar("/api/callcenter/citas", cita, client.personal("callcenter")).status)
        }
        probarConCatalogo(temporal, appwrite) {
            assertTrue(client.prefactura("1111111111", "FELIZ").contains("EN_REVISION"))
            val personal = client.personal("facturador")
            assertEquals("""{"escenarios":10}""", client.enviar("/api/dev/poblar", "{}", personal).bodyAsText())
            assertFalse(client.prefactura("1111111111", "FELIZ").contains("EN_REVISION"))
            assertEquals("SIN_CITA_HOY", client.enviar("/api/digiturno/llegadas", """{"identificacion":"1010101010"}""", personal).campo("motivo"))
        }
    }
}
