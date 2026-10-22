package com.hospital.facturacion.data

import com.hospital.facturacion.data.cita.AgendaCitasMemoria
import com.hospital.facturacion.data.cita.leerCitasPrueba
import com.hospital.facturacion.data.facturador.ClavePbkdf2
import com.hospital.facturacion.data.facturador.DirectorioFacturadoresMemoria
import com.hospital.facturacion.data.facturador.leerFacturadoresPrueba
import com.hospital.facturacion.data.formulario.leerFormulario
import com.hospital.facturacion.domain.facturador.Facturador
import com.hospital.facturacion.relojDePrueba
import kotlinx.serialization.json.Json
import java.nio.file.Path
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Lee los archivos reales de `config/`: si alguien los edita mal, esta prueba falla antes de arrancar. */
class ConfiguracionPruebaTest {
    @Test
    fun `las citas de prueba abren con identificacion e ID de acceso y fechan desde hoy`() {
        val agenda = AgendaCitasMemoria(leerCitasPrueba(Path.of("config/citas-prueba.json"), Json, relojDePrueba))
        val cita = agenda.buscarPorAcceso("1000000001", "PRUEBA1")
        assertEquals(LocalDate.now(relojDePrueba), cita?.fecha)
        assertNull(agenda.buscarPorAcceso("1000000001", "PRUEBA2"))
        assertNull(agenda.buscarPorAcceso("1000000001", "prueba1"))
    }

    @Test
    fun `el facturador de prueba entra con la clave documentada en config LEEME`() {
        val directorio = DirectorioFacturadoresMemoria(leerFacturadoresPrueba(Path.of("config/facturadores-prueba.json"), Json), ClavePbkdf2())
        assertEquals(Facturador("facturador", "Facturador de prueba"), directorio.autenticar("facturador", "prueba123"))
        assertNull(directorio.autenticar("facturador", "Prueba123"))
        assertNull(directorio.autenticar("otro", "prueba123"))
    }

    @Test
    fun `el formulario incluye los datos que la reunion confirmo como obligatorios`() {
        val obligatorios = leerFormulario(Path.of("config/formulario-prefactura.json"), Json).campos.filter { it.obligatorio }.map { it.clave }
        assertEquals(true, obligatorios.containsAll(listOf("nombre-padre", "nombre-madre")))
    }
}
