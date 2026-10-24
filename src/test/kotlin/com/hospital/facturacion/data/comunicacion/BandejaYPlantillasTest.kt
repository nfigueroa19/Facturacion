package com.hospital.facturacion.data.comunicacion

import com.hospital.facturacion.AppwriteDePrueba
import com.hospital.facturacion.PostgresDePrueba
import com.hospital.facturacion.citaIdDePrueba
import com.hospital.facturacion.domain.comunicacion.BandejaComunicaciones
import com.hospital.facturacion.domain.comunicacion.Celular
import com.hospital.facturacion.domain.comunicacion.Comunicacion
import com.hospital.facturacion.domain.comunicacion.EstadoEnvio
import com.hospital.facturacion.domain.comunicacion.MensajeSms
import com.hospital.facturacion.domain.comunicacion.MotivoComunicacion
import com.hospital.facturacion.domain.comunicacion.ResultadoEnvio
import com.hospital.facturacion.domain.contenido.ContenidoInstitucional
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class BandejaYPlantillasTest {
    private fun comunicacion(segundos: Long) = Comunicacion(
        citaIdDePrueba,
        MotivoComunicacion.CITA_ASIGNADA,
        MensajeSms(requireNotNull(Celular.de("3000000009")), "Mensaje $segundos"),
        ResultadoEnvio(EstadoEnvio.SIMULADO, "simulado"),
        Instant.parse("2026-10-22T13:00:00Z").plusSeconds(segundos),
    )

    private fun BandejaComunicaciones.verificarOrden() {
        registrar(comunicacion(0))
        registrar(comunicacion(60))
        assertEquals(listOf(comunicacion(60), comunicacion(0)), recientes())
    }

    @Test
    fun `la bandeja devuelve de la mas reciente a la mas antigua en PostgreSQL y en Appwrite`() {
        BandejaComunicacionesPostgres(PostgresDePrueba.baseMigrada()).verificarOrden()
        AppwriteDePrueba().use { BandejaComunicacionesAppwrite(it.tabla("comunicaciones", FilaComunicacion.serializer())).verificarOrden() }
    }

    @Test
    fun `la plantilla del contenido reemplaza las marcas con los valores`() {
        val contenido = ContenidoInstitucional(mapOf("sms.NOVEDAD_REGISTRADA" to "Solicitud {consecutivo} con novedades"), emptyMap())
        val texto = PlantillasSmsContenido { contenido }.texto(MotivoComunicacion.NOVEDAD_REGISTRADA, mapOf("consecutivo" to "PF-1"))
        assertEquals("Solicitud PF-1 con novedades", texto)
    }
}
