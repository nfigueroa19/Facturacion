package com.hospital.facturacion.data.cita

import com.hospital.facturacion.AppwriteDePrueba
import com.hospital.facturacion.citaConCopago
import com.hospital.facturacion.citaSinCopago
import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.comunicacion.Celular
import org.junit.jupiter.api.AfterEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AgendaCitasAppwriteTest {
    private val appwrite = AppwriteDePrueba()
    private val citas = appwrite.tabla("citas", FilaCita.serializer())
    private val otraCita = citaSinCopago.copy(id = requireNotNull(CitaId.de("cita-2")), identificacionPaciente = "1000000002")

    @AfterEach
    fun cerrar() = appwrite.close()

    @Test
    fun `siembra sin duplicar y encuentra la cita por ID o por identificacion y codigo de acceso`() {
        repeat(2) { sembrarCitas(citas, listOf(CitaRegistrada(citaConCopago, "CODIGO1"), CitaRegistrada(otraCita, "CODIGO2"))) }
        val agenda = AgendaCitasAppwrite(citas)
        assertEquals(2, appwrite.tablas.de("citas").size)
        assertEquals(otraCita, agenda.buscar(otraCita.id))
        assertEquals(citaConCopago, agenda.buscarPorAcceso("1000000001", "CODIGO1"))
        assertNull(agenda.buscarPorAcceso("1000000001", "CODIGO2"))
        assertNull(agenda.buscar(requireNotNull(CitaId.de("otra"))))
    }

    @Test
    fun `registra la cita del call center con su celular y la encuentra por identificacion`() {
        val agenda = AgendaCitasAppwrite(citas)
        val conCelular = otraCita.copy(celular = Celular.de("3000000002"))
        agenda.registrar(conCelular, "CODIGO2")
        assertEquals(listOf(conCelular), agenda.buscarPorIdentificacion("1000000002"))
        assertEquals(conCelular, agenda.buscarPorAcceso("1000000002", "CODIGO2"))
        assertEquals(emptyList(), agenda.buscarPorIdentificacion("1000000001"))
    }
}
