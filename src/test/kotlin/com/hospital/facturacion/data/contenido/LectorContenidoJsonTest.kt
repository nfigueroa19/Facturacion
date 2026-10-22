package com.hospital.facturacion.data.contenido

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class LectorContenidoJsonTest {
    @TempDir
    lateinit var temporal: Path

    private fun leer(contenido: String): LecturaContenido {
        val archivo = Files.writeString(temporal.resolve("contenido.json"), contenido)
        return LectorContenidoJson(archivo, Json).leer()
    }

    @Test
    fun `acepta UTF-8 con BOM como lo guarda el Bloc de notas`() {
        val lectura = assertIs<LecturaContenido.Valida>(leer(Character.toString(0xFEFF) + """{"textos":{"titulo":"Prefactura"}}"""))
        assertEquals(mapOf("titulo" to "Prefactura"), lectura.contenido.textos)
    }

    @Test
    fun `marca como invalido un JSON roto, un campo desconocido o un nombre de archivo con ruta`() {
        listOf("""{"textos":{"titulo":}""", """{"textoss":{}}""", """{"imagenes":{"logo":"../secreto.png"}}""")
            .forEach { assertIs<LecturaContenido.Invalida>(leer(it), it) }
    }
}
