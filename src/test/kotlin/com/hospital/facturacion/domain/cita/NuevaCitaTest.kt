package com.hospital.facturacion.domain.cita

import com.hospital.facturacion.citaIdDePrueba
import com.hospital.facturacion.domain.comunicacion.Celular
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NuevaCitaTest {
    private val hoy = LocalDate.of(2026, 10, 22)
    private val datos = DatosNuevaCita(" 1000000009 ", "300 000 0009", "2026-10-22", " Médico ", "Medicina general", Regimen.CONTRIBUTIVO, 5000)

    @Test
    fun `crea la cita con los datos limpios, el copago activado y el celular`() {
        val cita = assertIs<ValidacionNuevaCita.Valida>(datos.validar(hoy, citaIdDePrueba)).cita
        val esperada = Cita(citaIdDePrueba, "1000000009", hoy, "Médico", "Medicina general", Regimen.CONTRIBUTIVO, Copago(5000), Celular.de("3000000009"))
        assertEquals(esperada, cita)
    }

    @Test
    fun `reporta cada dato con formato invalido y no acepta fechas pasadas`() {
        val errados = DatosNuevaCita("12", "6011234567", "2026-10-21", "", " ", Regimen.SUBSIDIADO, 0)
        assertEquals(ErrorNuevaCita.entries.toSet(), assertIs<ValidacionNuevaCita.Invalida>(errados.validar(hoy, citaIdDePrueba)).errores)
        assertEquals(setOf(ErrorNuevaCita.FECHA), assertIs<ValidacionNuevaCita.Invalida>(datos.copy(fecha = "22/10/2026").validar(hoy, citaIdDePrueba)).errores)
        assertIs<ValidacionNuevaCita.Valida>(datos.copy(copagoPesos = null).validar(hoy, citaIdDePrueba))
    }

    @Test
    fun `el celular es movil colombiano de diez digitos y el dia de la cita sigue la zona del reloj`() {
        assertNull(Celular.de("+573000000009"))
        assertNull(Celular.de("30000000"))
        val cita = Cita(citaIdDePrueba, "1", hoy, "M", "E", Regimen.CONTRIBUTIVO, null)
        assertTrue(cita.esHoy(Clock.fixed(Instant.parse("2026-10-22T23:00:00Z"), ZoneOffset.UTC)))
        assertFalse(cita.esHoy(Clock.fixed(Instant.parse("2026-10-23T01:00:00Z"), ZoneOffset.UTC)))
        assertTrue(cita.esHoy(Clock.fixed(Instant.parse("2026-10-23T01:00:00Z"), ZoneOffset.ofHours(-5))))
    }
}
