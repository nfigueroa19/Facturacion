package com.hospital.facturacion.data.appwrite

import com.hospital.facturacion.AppwriteDePrueba
import com.hospital.facturacion.data.appwrite.ConsultaAppwrite.Companion.igual
import kotlinx.serialization.json.JsonObject
import org.junit.jupiter.api.AfterEach
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/** Reintentos de [TablaAppwrite] ante las fallas transitorias que imita [com.hospital.facturacion.TablasDePrueba]. */
class TablaAppwriteReintentosTest {
    private val appwrite = AppwriteDePrueba()
    private val tabla = appwrite.tabla("prueba", FilaPrueba.serializer())

    @AfterEach
    fun cerrar() = appwrite.close()

    @Test
    fun `reintenta con espera exponencial cuando Appwrite responde vacio al guardar o falla el servidor`() {
        appwrite.tablas.guardadosSinConfirmar.set(2)
        tabla.guardar("a", FilaPrueba("x", "1"))
        assertEquals(FilaPrueba("x", "1"), tabla.leer("a"))
        assertEquals(listOf(Duration.ofMillis(100), Duration.ofMillis(200)), appwrite.esperas)
        appwrite.tablas.fallosServidor.set(1)
        assertEquals(listOf("1"), tabla.buscar(igual("grupo", "x")).map { it.orden })
    }

    @Test
    fun `falla agotados los intentos, sin reintentar incrementos ni consultas que no caben en una pagina`() {
        appwrite.tablas.guardadosSinConfirmar.set(3)
        assertFailsWith<FalloTransitorioAppwrite> { tabla.guardar("a", FilaPrueba("x", "1")) }
        assertNull(tabla.leer("a"))
        tabla.guardar("a", FilaPrueba("x", "1"))
        appwrite.esperas.clear()
        appwrite.tablas.fallosServidor.set(1)
        assertFailsWith<FalloTransitorioAppwrite> { tabla.incrementar("a", "valor") }
        repeat(MAXIMO_FILAS) { appwrite.tablas.de("prueba")["f$it"] = JsonObject(appwrite.tablas.de("prueba").getValue("a")) }
        assertFailsWith<IllegalStateException> { tabla.buscar() }
        assertEquals(emptyList(), appwrite.esperas)
    }
}
