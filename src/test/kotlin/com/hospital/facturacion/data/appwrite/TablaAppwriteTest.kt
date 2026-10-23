package com.hospital.facturacion.data.appwrite

import com.hospital.facturacion.AppwriteDePrueba
import com.hospital.facturacion.data.appwrite.ConsultaAppwrite.Companion.ascendente
import com.hospital.facturacion.data.appwrite.ConsultaAppwrite.Companion.igual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import org.junit.jupiter.api.AfterEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@Serializable
data class FilaPrueba(val grupo: String, val orden: String, val valor: Long = 0)

class TablaAppwriteTest {
    private val appwrite = AppwriteDePrueba()
    private val tabla = appwrite.tabla("prueba", FilaPrueba.serializer())

    @AfterEach
    fun cerrar() = appwrite.close()

    @Test
    fun `guarda, reemplaza, filtra en orden e incrementa en el servidor`() {
        tabla.guardar("b", FilaPrueba("x", "2"))
        tabla.guardar("a", FilaPrueba("x", "1"))
        tabla.guardar("c", FilaPrueba("y", "0"))
        tabla.guardar("c", FilaPrueba("y", "3"))
        assertEquals(FilaPrueba("y", "3"), tabla.leer("c"))
        assertNull(tabla.leer("d"))
        assertEquals(listOf("1", "2"), tabla.buscar(igual("grupo", "x"), ascendente("orden")).map { it.orden })
        assertEquals(listOf(1L, 2L), List(2) { tabla.incrementar("a", "valor").valor })
    }

    @Test
    fun `falla si Appwrite no confirma la fila o si la consulta no cabe en una pagina`() {
        appwrite.tablas.confirmarGuardado = false
        assertFailsWith<IllegalStateException> { tabla.guardar("a", FilaPrueba("x", "1")) }
        repeat(MAXIMO_FILAS) { appwrite.tablas.de("prueba")["f$it"] = JsonObject(appwrite.tablas.de("prueba").getValue("a")) }
        assertFailsWith<IllegalStateException> { tabla.buscar() }
    }
}
