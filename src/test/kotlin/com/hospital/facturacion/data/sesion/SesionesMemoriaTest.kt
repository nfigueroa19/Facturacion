package com.hospital.facturacion.data.sesion

import com.hospital.facturacion.domain.sesion.TokenSesion
import java.security.SecureRandom
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class SesionesMemoriaTest {
    private var ahora = Instant.parse("2026-10-22T08:00:00Z")
    private val reloj = object : Clock() {
        override fun instant() = ahora

        override fun getZone(): ZoneId = ZoneOffset.UTC

        override fun withZone(zone: ZoneId) = this
    }
    private val sesiones = SesionesMemoria<String>(reloj, Duration.ofMinutes(30), SecureRandom())

    @Test
    fun `entrega tokens distintos de 256 bits y los olvida al vencer`() {
        val token = sesiones.abrir("cita-1")
        assertNotEquals(token, sesiones.abrir("cita-1"))
        assertEquals(43, token.valor.length)
        ahora = ahora.plus(Duration.ofMinutes(29))
        assertEquals("cita-1", sesiones.sujeto(token))
        ahora = ahora.plus(Duration.ofMinutes(1))
        assertNull(sesiones.sujeto(token))
        assertNull(sesiones.sujeto(TokenSesion("inventado")))
    }
}
