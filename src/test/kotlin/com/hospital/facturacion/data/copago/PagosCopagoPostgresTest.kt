package com.hospital.facturacion.data.copago

import com.hospital.facturacion.PostgresDePrueba
import com.hospital.facturacion.citaIdDePrueba
import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.cita.Copago
import com.hospital.facturacion.domain.copago.PagoCopago
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PagosCopagoPostgresTest {
    @Test
    fun `registra un pago por cita y el ultimo reemplaza al anterior`() {
        val pagos = PagosCopagoPostgres(PostgresDePrueba.baseMigrada())
        val pago = PagoCopago("REF-1", Copago(5000), Instant.parse("2026-10-22T13:00:00Z"))
        pagos.guardar(citaIdDePrueba, pago)
        pagos.guardar(citaIdDePrueba, pago.copy(referencia = "REF-2"))
        assertEquals(pago.copy(referencia = "REF-2"), pagos.buscar(citaIdDePrueba))
        assertNull(pagos.buscar(requireNotNull(CitaId.de("otra"))))
    }
}
