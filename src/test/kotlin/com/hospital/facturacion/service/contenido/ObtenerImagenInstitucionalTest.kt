package com.hospital.facturacion.service.contenido

import com.hospital.facturacion.ImagenesDePrueba
import com.hospital.facturacion.domain.contenido.AlmacenImagenes
import com.hospital.facturacion.domain.contenido.ClaveRecurso
import com.hospital.facturacion.domain.contenido.ContenidoInstitucional
import com.hospital.facturacion.domain.contenido.FuenteContenido
import com.hospital.facturacion.domain.contenido.LimpiadorMetadatos
import com.hospital.facturacion.domain.contenido.NombreArchivoImagen
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ObtenerImagenInstitucionalTest {
    private val clave = requireNotNull(ClaveRecurso.de("logo-encabezado"))
    private val fuente = FuenteContenido {
        ContenidoInstitucional(emptyMap(), mapOf(clave to requireNotNull(NombreArchivoImagen.de("logo.png"))))
    }
    private val png = ImagenesDePrueba.png()

    private fun caso(almacen: AlmacenImagenes, limpiador: LimpiadorMetadatos = LimpiadorMetadatos { it }) =
        ObtenerImagenInstitucional(fuente, almacen, limpiador)

    @Test
    fun `clasifica cada rechazo`() {
        assertEquals(ResultadoImagen.NoRegistrada, caso(AlmacenImagenes { png }).ejecutar(requireNotNull(ClaveRecurso.de("otra"))))
        assertEquals(ResultadoImagen.NoDisponible, caso(AlmacenImagenes { null }).ejecutar(clave))
        assertEquals(ResultadoImagen.Rechazada, caso(AlmacenImagenes { "<svg>".toByteArray() }).ejecutar(clave))
        assertEquals(ResultadoImagen.Rechazada, caso(AlmacenImagenes { png }) { null }.ejecutar(clave))
    }

    @Test
    fun `entrega la imagen que devuelve el limpiador`() {
        assertIs<ResultadoImagen.Encontrada>(caso(AlmacenImagenes { png }).ejecutar(clave))
    }
}
