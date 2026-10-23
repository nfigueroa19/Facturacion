package com.hospital.facturacion.data.copago

import com.hospital.facturacion.AppwriteDePrueba
import com.hospital.facturacion.citaIdDePrueba
import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.cita.Copago
import com.hospital.facturacion.domain.copago.PagoCopago
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PagosCopagoAppwriteTest {
    @Test
    fun `registra un pago por cita y el ultimo reemplaza al anterior`() = AppwriteDePrueba().use { appwrite ->
        val pagos = PagosCopagoAppwrite(appwrite.tabla("pagos_copago", FilaPago.serializer()))
        val pago = PagoCopago("REF-1", Copago(5000), Instant.parse("2026-10-22T13:00:00.123456Z"))
        pagos.guardar(citaIdDePrueba, pago)
        pagos.guardar(citaIdDePrueba, pago.copy(referencia = "REF-2"))
        assertEquals(PagoCopago("REF-2", Copago(5000), Instant.parse("2026-10-22T13:00:00.123Z")), pagos.buscar(citaIdDePrueba))
        assertNull(pagos.buscar(requireNotNull(CitaId.de("otra"))))
    }
}
