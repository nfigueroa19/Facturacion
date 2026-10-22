package com.hospital.facturacion.domain.contenido

import com.hospital.facturacion.ImagenesDePrueba
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ImagenInstitucionalTest {
    @Test
    fun `acepta contenido cuya firma coincide con el formato`() {
        assertNotNull(ImagenInstitucional.de(FormatoImagen.PNG, ImagenesDePrueba.png()))
    }

    @Test
    fun `rechaza contenido que no corresponde a la extension`() {
        assertNull(ImagenInstitucional.de(FormatoImagen.JPEG, ImagenesDePrueba.png()))
        assertNull(ImagenInstitucional.de(FormatoImagen.PNG, "<svg onload=alert(1)>".toByteArray()))
        assertNull(ImagenInstitucional.de(FormatoImagen.PNG, ByteArray(0)))
    }
}
