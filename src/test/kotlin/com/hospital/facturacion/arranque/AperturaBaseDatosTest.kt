package com.hospital.facturacion.arranque

import com.hospital.facturacion.AlmacenesDePrueba
import com.hospital.facturacion.data.secretos.AlmacenSecretosPkcs12
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFailsWith

class AperturaBaseDatosTest {
    @TempDir
    lateinit var temporal: Path

    private val configuracion = ConfiguracionBaseDatos("jdbc:postgresql://localhost:1/x", "facturacion", "basedatos.clave", 1)

    @Test
    fun `no arranca sin almacen de secretos ni sin el alias de la clave`() {
        val sinAlmacen = assertFailsWith<IllegalArgumentException> { abrirBaseDatos(configuracion, null) }
        assertContains(sinAlmacen.message.orEmpty(), "HSLV_SECRETOS_ALMACEN")
        val ruta = AlmacenesDePrueba.crear(temporal.resolve("s.p12"), "clave".toCharArray(), mapOf("otro" to "x"))
        val sinAlias = assertFailsWith<IllegalArgumentException> {
            abrirBaseDatos(configuracion, AlmacenSecretosPkcs12(ruta, "clave".toCharArray()))
        }
        assertContains(sinAlias.message.orEmpty(), "basedatos.clave")
    }
}
