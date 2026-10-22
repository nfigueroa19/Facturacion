package com.hospital.facturacion.domain.cita

import com.hospital.facturacion.citaConCopago
import com.hospital.facturacion.citaSinCopago
import com.hospital.facturacion.domain.prefactura.ItemRevision
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CitaTest {
    @Test
    fun `el identificador de cita solo admite minusculas, numeros y guiones porque forma nombres de archivo`() {
        assertNotNull(CitaId.de("cita-prueba-001"))
        listOf("", "../x", "Cita", "cita_1", "cita-", "a\\b", "x".repeat(41)).forEach { assertNull(CitaId.de(it), it) }
    }

    @Test
    fun `el copago es positivo`() {
        assertFailsWith<IllegalArgumentException> { Copago(0) }
    }

    @Test
    fun `el checklist pide el copago solo si la cita lo tiene activo`() {
        assertEquals(ItemRevision.entries.toSet(), ItemRevision.requeridosPara(citaConCopago))
        assertEquals(ItemRevision.entries.toSet() - ItemRevision.COPAGO, ItemRevision.requeridosPara(citaSinCopago))
    }
}
