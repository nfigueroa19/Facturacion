package com.hospital.facturacion.service.callcenter

import com.hospital.facturacion.NotificadorDePrueba
import com.hospital.facturacion.citaIdDePrueba
import com.hospital.facturacion.data.cita.AgendaCitasMemoria
import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.cita.DatosNuevaCita
import com.hospital.facturacion.domain.cita.ErrorNuevaCita
import com.hospital.facturacion.domain.cita.GeneradorAcceso
import com.hospital.facturacion.domain.cita.Regimen
import com.hospital.facturacion.domain.comunicacion.MotivoComunicacion
import com.hospital.facturacion.relojDePrueba
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ProgramarCitaTest {
    private val agenda = AgendaCitasMemoria(emptyList())
    private val notificador = NotificadorDePrueba()
    private val generador = object : GeneradorAcceso {
        override fun citaId(): CitaId = citaIdDePrueba

        override fun codigoAcceso() = "ABCD2345"
    }
    private val programar = ProgramarCita(agenda, generador, notificador, relojDePrueba)
    private val datos = DatosNuevaCita("1000000009", "3000000009", "2026-10-22", "Médico", "Medicina general", Regimen.SUBSIDIADO, null)

    @Test
    fun `registra la cita con su ID de acceso y envia el SMS con fecha, medico y codigo`() {
        val programada = assertIs<ResultadoProgramacion.Programada>(programar.ejecutar(datos))
        assertEquals("ABCD2345", programada.codigoAcceso)
        assertEquals(programada.cita, agenda.buscarPorAcceso("1000000009", "ABCD2345"))
        val (cita, motivo, valores) = notificador.avisos.single()
        assertEquals(programada.cita to MotivoComunicacion.CITA_ASIGNADA, cita to motivo)
        assertEquals(mapOf("fecha" to "2026-10-22", "medico" to "Médico", "especialidad" to "Medicina general", "codigo" to "ABCD2345"), valores)
    }

    @Test
    fun `no registra ni avisa si un dato es invalido`() {
        val invalida = assertIs<ResultadoProgramacion.Invalida>(programar.ejecutar(datos.copy(celular = "123")))
        assertEquals(setOf(ErrorNuevaCita.CELULAR), invalida.errores)
        assertEquals(emptyList(), agenda.buscarPorIdentificacion("1000000009"))
        assertEquals(emptyList(), notificador.avisos)
    }
}
