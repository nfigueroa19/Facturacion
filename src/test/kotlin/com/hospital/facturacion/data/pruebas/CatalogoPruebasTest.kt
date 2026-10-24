package com.hospital.facturacion.data.pruebas

import com.hospital.facturacion.data.facturador.ClavePbkdf2
import com.hospital.facturacion.data.facturador.DirectorioFacturadoresMemoria
import com.hospital.facturacion.data.facturador.GeneradorHashPbkdf2
import com.hospital.facturacion.data.formulario.leerFormulario
import com.hospital.facturacion.relojDePrueba
import kotlinx.serialization.json.Json
import java.nio.file.Path
import java.security.SecureRandom
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

/** Lee el `config/usuarios-prueba.json` real: si alguien lo edita mal, esta prueba falla antes de arrancar. */
class CatalogoPruebasTest {
    @Test
    fun `el catalogo de pruebas trae personal que entra con su clave, datos de formulario completos y escenarios unicos`() {
        val catalogo = leerCatalogoPruebas(Path.of("config/usuarios-prueba.json"), Json)
        val directorio = DirectorioFacturadoresMemoria(catalogo.facturadores(GeneradorHashPbkdf2(SecureRandom())), ClavePbkdf2())
        catalogo.personal.forEach { assertEquals(it.usuario, directorio.autenticar(it.usuario, it.clave)?.usuario) }
        val obligatorios = leerFormulario(Path.of("config/formulario-prefactura.json"), Json).campos.filter { it.obligatorio }.map { it.clave }
        assertEquals(emptyList(), obligatorios - catalogo.datosFormulario.keys)
        val identificaciones = catalogo.pacientes.map { it.identificacion } + catalogo.callcenter.map { it.identificacion }
        assertEquals(identificaciones.size, identificaciones.toSet().size)
        catalogo.pacientes.forEach { it.cita(LocalDate.now(relojDePrueba)) }
    }
}
