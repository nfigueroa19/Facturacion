package com.hospital.facturacion.data

import com.hospital.facturacion.AppwriteDePrueba
import com.hospital.facturacion.PostgresDePrueba
import com.hospital.facturacion.citaConCopago
import com.hospital.facturacion.data.cita.GeneradorAccesoAleatorio
import com.hospital.facturacion.data.copago.PasarelaPagosSimulada
import com.hospital.facturacion.data.digiturno.GeneradorTurnosAppwrite
import com.hospital.facturacion.data.digiturno.GeneradorTurnosPostgres
import com.hospital.facturacion.data.digiturno.prepararTurnos
import com.hospital.facturacion.data.prefactura.FilaConsecutivo
import com.hospital.facturacion.domain.cita.Copago
import com.hospital.facturacion.domain.copago.MotivoRechazoPago
import com.hospital.facturacion.domain.copago.ResultadoPasarela
import com.hospital.facturacion.domain.copago.TokenPago
import com.hospital.facturacion.domain.digiturno.DestinoTurno
import com.hospital.facturacion.domain.digiturno.GeneradorTurnos
import java.security.SecureRandom
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GeneradoresTest {
    private fun GeneradorTurnos.verificar() {
        assertEquals("P-001", siguiente(DestinoTurno.PRECONSULTA).codigo)
        assertEquals("F-001", siguiente(DestinoTurno.FACTURACION).codigo)
        assertEquals("P-002", siguiente(DestinoTurno.PRECONSULTA).codigo)
    }

    @Test
    fun `los turnos llevan un contador por destino en PostgreSQL y en Appwrite`() {
        GeneradorTurnosPostgres(PostgresDePrueba.baseMigrada()).verificar()
        AppwriteDePrueba().use { appwrite ->
            val consecutivos = appwrite.tabla("consecutivos", FilaConsecutivo.serializer())
            prepararTurnos(consecutivos)
            GeneradorTurnosAppwrite(consecutivos).verificar()
            prepararTurnos(consecutivos)
            assertEquals("F-002", GeneradorTurnosAppwrite(consecutivos).siguiente(DestinoTurno.FACTURACION).codigo)
        }
    }

    @Test
    fun `el ID de acceso no usa caracteres que se confunden y el ID de cita es valido`() {
        val generador = GeneradorAccesoAleatorio(SecureRandom())
        repeat(50) { assertTrue(Regex("[A-HJ-NP-Z2-9]{8}").matches(generador.codigoAcceso())) }
        assertTrue(generador.citaId().valor.matches(Regex("cita-[0-9a-f]{12}")))
    }
}
