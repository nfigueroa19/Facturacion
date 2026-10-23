package com.hospital.facturacion.data.facturador

import com.hospital.facturacion.AppwriteDePrueba
import com.hospital.facturacion.domain.facturador.Facturador
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DirectorioFacturadoresAppwriteTest {
    @Test
    fun `siembra los facturadores del archivo sin duplicarlos y autentica contra el hash guardado`() = AppwriteDePrueba().use { appwrite ->
        val tabla = appwrite.tabla("usuarios", FilaUsuario.serializer())
        repeat(2) { sembrarFacturadores(tabla, leerFacturadoresPrueba(Path.of("config/facturadores-prueba.json"), Json)) }
        val directorio = DirectorioFacturadoresAppwrite(tabla, ClavePbkdf2())
        assertEquals(1, appwrite.tablas.de("usuarios").size)
        assertEquals(Facturador("facturador", "Facturador de prueba"), directorio.autenticar("facturador", "prueba123"))
        assertNull(directorio.autenticar("facturador", "Prueba123"))
        assertNull(directorio.autenticar("otro", "prueba123"))
    }
}
