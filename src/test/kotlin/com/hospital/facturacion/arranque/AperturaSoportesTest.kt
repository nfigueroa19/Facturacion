package com.hospital.facturacion.arranque

import com.hospital.facturacion.AlmacenesDePrueba
import com.hospital.facturacion.AppwriteDePrueba
import com.hospital.facturacion.data.secretos.AlmacenSecretosPkcs12
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class AperturaSoportesTest {
    @TempDir
    lateinit var temporal: Path
    private val appwrite = AppwriteDePrueba()

    @AfterEach
    fun cerrar() = appwrite.close()

    private fun almacen(secretos: Map<String, String>) =
        AlmacenSecretosPkcs12(AlmacenesDePrueba.crear(temporal.resolve("s.p12"), "clave".toCharArray(), secretos), "clave".toCharArray())

    @Test
    fun `lee endpoint, proyecto y clave de Appwrite del almacen sin exponer la clave`() {
        val bucket = abrirAppwrite("soportes", almacen(appwrite.secretos()))
        assertContains(bucket.toString(), appwrite.proyecto)
        assertFalse(bucket.toString().contains(appwrite.claveApi))
    }

    @Test
    fun `no arranca con Appwrite sin almacen de secretos ni sin cada alias`() {
        val sinAlmacen = assertFailsWith<IllegalArgumentException> { abrirAppwrite("soportes", null) }
        assertContains(sinAlmacen.message.orEmpty(), "HSLV_SECRETOS_ALMACEN")
        val sinClave = assertFailsWith<IllegalArgumentException> {
            abrirAppwrite("soportes", almacen(appwrite.secretos() - ALIAS_APPWRITE_CLAVE))
        }
        assertContains(sinClave.message.orEmpty(), ALIAS_APPWRITE_CLAVE)
    }
}
