package com.hospital.facturacion.data.soporte

import com.hospital.facturacion.AppwriteDePrueba
import com.hospital.facturacion.BUCKET_APPWRITE_PRUEBA
import com.hospital.facturacion.citaIdDePrueba
import com.hospital.facturacion.domain.soporte.TipoSoporte
import com.hospital.facturacion.pdfDePrueba
import org.junit.jupiter.api.AfterEach
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class AlmacenSoportesAppwriteTest {
    private val appwrite = AppwriteDePrueba()
    private val almacen = AlmacenSoportesAppwrite(appwrite.cliente(), BUCKET_APPWRITE_PRUEBA, 1024)

    @AfterEach
    fun cerrar() = appwrite.close()

    @Test
    fun `guarda el PDF sin modificar y lo reemplaza al cargar de nuevo`() {
        almacen.guardar(citaIdDePrueba, TipoSoporte.ORDEN_MEDICA, pdfDePrueba("%PDF-1 primera"))
        almacen.guardar(citaIdDePrueba, TipoSoporte.ORDEN_MEDICA, pdfDePrueba("%PDF-1 segunda"))
        assertEquals("%PDF-1 segunda", almacen.leer(citaIdDePrueba, TipoSoporte.ORDEN_MEDICA)?.contenido?.decodeToString())
        assertEquals("cita-1-orden_medica.pdf", appwrite.archivos.values.single().nombre)
        assertEquals(setOf(TipoSoporte.ORDEN_MEDICA), almacen.cargados(citaIdDePrueba))
        assertNull(almacen.leer(citaIdDePrueba, TipoSoporte.AUTORIZACION))
    }

    @Test
    fun `valida de nuevo al leer y no entrega archivos alterados que ya no son PDF`() {
        almacen.guardar(citaIdDePrueba, TipoSoporte.AUTORIZACION, pdfDePrueba())
        appwrite.archivos.replaceAll { _, archivo -> archivo.copy(contenido = "<html>".toByteArray()) }
        assertNull(almacen.leer(citaIdDePrueba, TipoSoporte.AUTORIZACION))
    }

    @Test
    fun `falla sin guardar nada si Appwrite rechaza la clave y no admite limites que exigen subir por partes`() {
        val sinPermiso = AlmacenSoportesAppwrite(appwrite.cliente("clave-equivocada"), BUCKET_APPWRITE_PRUEBA, 1024)
        val error = assertFailsWith<IllegalStateException> { sinPermiso.guardar(citaIdDePrueba, TipoSoporte.AUTORIZACION, pdfDePrueba()) }
        assertContains(error.message.orEmpty(), "401")
        assertEquals(emptyMap(), appwrite.archivos)
        assertFailsWith<IllegalArgumentException> { AlmacenSoportesAppwrite(appwrite.cliente(), BUCKET_APPWRITE_PRUEBA, 5 * 1024 * 1024 + 1) }
    }
}
