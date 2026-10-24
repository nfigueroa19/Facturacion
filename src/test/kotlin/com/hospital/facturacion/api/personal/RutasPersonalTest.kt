package com.hospital.facturacion.api.personal

import com.hospital.facturacion.arranque.campo
import com.hospital.facturacion.arranque.enviar
import com.hospital.facturacion.arranque.probarModulo
import com.hospital.facturacion.arranque.tokenFacturador
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Call center, digiturno y panel de comunicaciones con el módulo completo. Los SMS se simulan. */
class RutasPersonalTest {
    @TempDir
    lateinit var temporal: Path

    private val hoy = LocalDate.now(ZoneId.of("America/Bogota"))

    @Test
    fun `el call center programa la cita, el SMS queda simulado y el paciente ingresa con el ID generado`() = probarModulo(temporal) {
        val personal = client.tokenFacturador()
        val cita = """{"identificacion":"1000000099","celular":"3000000099","fecha":"$hoy","medico":"Médico","especialidad":"General","regimen":"SUBSIDIADO"}"""
        val programada = client.enviar("/api/callcenter/citas", cita, personal)
        assertEquals(HttpStatusCode.Created, programada.status)
        val codigo = programada.campo("codigoAcceso")
        assertTrue(programada.bodyAsText().contains(""""estado":"SIMULADO"""") && programada.bodyAsText().contains(codigo))
        val sesion = client.enviar("/api/paciente/sesion", """{"identificacion":"1000000099","codigoAcceso":"$codigo"}""")
        assertEquals(HttpStatusCode.OK, sesion.status)
        val comunicaciones = client.get("/api/personal/comunicaciones") { bearerAuth(personal) }.bodyAsText()
        assertTrue(comunicaciones.contains("CITA_ASIGNADA") && comunicaciones.contains("3000000099"))
        val invalida = client.enviar("/api/callcenter/citas", cita.replace("3000000099", "12"), personal)
        assertEquals(HttpStatusCode.UnprocessableEntity to """{"errores":["CELULAR"]}""", invalida.status to invalida.bodyAsText())
    }

    @Test
    fun `el digiturno envia a ventanilla a quien no ha radicado y nada responde sin sesion del personal`() = probarModulo(temporal) {
        val turno = client.enviar("/api/digiturno/llegadas", """{"identificacion":"1000000001"}""", client.tokenFacturador())
        assertEquals("F-001" to "SIN_RADICAR", turno.campo("turno") to turno.campo("motivo"))
        assertEquals(HttpStatusCode.Unauthorized, client.enviar("/api/digiturno/llegadas", """{"identificacion":"1000000001"}""").status)
        assertEquals(HttpStatusCode.Unauthorized, client.enviar("/api/callcenter/citas", "{}").status)
        assertEquals(HttpStatusCode.Unauthorized, client.get("/api/personal/comunicaciones").status)
    }
}
