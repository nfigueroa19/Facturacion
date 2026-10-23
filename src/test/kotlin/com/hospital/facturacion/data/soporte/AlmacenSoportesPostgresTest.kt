package com.hospital.facturacion.data.soporte

import com.hospital.facturacion.PostgresDePrueba
import com.hospital.facturacion.citaIdDePrueba
import com.hospital.facturacion.data.postgres.ejecutar
import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.soporte.TipoSoporte
import com.hospital.facturacion.pdfDePrueba
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AlmacenSoportesPostgresTest {
    private val base = PostgresDePrueba.baseMigrada()
    private val almacen = AlmacenSoportesPostgres(base, 1024)

    @Test
    fun `guarda el PDF sin modificar y lo reemplaza al cargar de nuevo`() {
        almacen.guardar(citaIdDePrueba, TipoSoporte.ORDEN_MEDICA, pdfDePrueba("%PDF-1 primera"))
        almacen.guardar(citaIdDePrueba, TipoSoporte.ORDEN_MEDICA, pdfDePrueba("%PDF-1 segunda"))
        assertEquals("%PDF-1 segunda", almacen.leer(citaIdDePrueba, TipoSoporte.ORDEN_MEDICA)?.contenido?.decodeToString())
        assertEquals(setOf(TipoSoporte.ORDEN_MEDICA), almacen.cargados(citaIdDePrueba))
        assertEquals(emptySet(), almacen.cargados(requireNotNull(CitaId.de("otra"))))
    }

    @Test
    fun `no entrega filas alteradas que ya no son PDF o superan el limite`() {
        almacen.guardar(citaIdDePrueba, TipoSoporte.AUTORIZACION, pdfDePrueba("%PDF-1 grande"))
        assertNull(AlmacenSoportesPostgres(base, 4).leer(citaIdDePrueba, TipoSoporte.AUTORIZACION))
        base.ejecutar("UPDATE soporte SET contenido = ?", "<html>".toByteArray())
        assertNull(almacen.leer(citaIdDePrueba, TipoSoporte.AUTORIZACION))
    }
}
