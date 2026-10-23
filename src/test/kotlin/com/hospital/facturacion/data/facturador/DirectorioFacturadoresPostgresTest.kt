package com.hospital.facturacion.data.facturador

import com.hospital.facturacion.PostgresDePrueba
import com.hospital.facturacion.domain.facturador.Facturador
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DirectorioFacturadoresPostgresTest {
    @Test
    fun `siembra los facturadores del archivo sin duplicarlos y autentica contra el hash guardado`() {
        val base = PostgresDePrueba.baseMigrada()
        val usuarios = leerFacturadoresPrueba(Path.of("config/facturadores-prueba.json"), Json)
        repeat(2) { sembrarFacturadores(base, usuarios) }
        val directorio = DirectorioFacturadoresPostgres(base, ClavePbkdf2())
        assertEquals(Facturador("facturador", "Facturador de prueba"), directorio.autenticar("facturador", "prueba123"))
        assertNull(directorio.autenticar("facturador", "Prueba123"))
        assertNull(directorio.autenticar("otro", "prueba123"))
    }
}
