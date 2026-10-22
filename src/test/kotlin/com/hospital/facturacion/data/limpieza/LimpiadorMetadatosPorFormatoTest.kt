package com.hospital.facturacion.data.limpieza

import com.hospital.facturacion.ImagenesDePrueba
import com.hospital.facturacion.domain.contenido.FormatoImagen
import com.hospital.facturacion.domain.contenido.ImagenInstitucional
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class LimpiadorMetadatosPorFormatoTest {
    private val png = assertNotNull(ImagenInstitucional.de(FormatoImagen.PNG, ImagenesDePrueba.png()))

    @Test
    fun `usa el limpiador del formato de la imagen`() {
        assertNotNull(LimpiadorMetadatosPorFormato(listOf(LimpiadorPng())).limpiar(png))
    }

    @Test
    fun `no entrega imagenes de formatos sin limpiador`() {
        assertNull(LimpiadorMetadatosPorFormato(listOf(LimpiadorJpeg())).limpiar(png))
    }
}
