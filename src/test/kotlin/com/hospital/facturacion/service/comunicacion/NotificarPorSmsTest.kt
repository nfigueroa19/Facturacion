package com.hospital.facturacion.service.comunicacion

import com.hospital.facturacion.citaConCopago
import com.hospital.facturacion.data.comunicacion.BandejaComunicacionesMemoria
import com.hospital.facturacion.domain.comunicacion.CanalSms
import com.hospital.facturacion.domain.comunicacion.Celular
import com.hospital.facturacion.domain.comunicacion.Comunicacion
import com.hospital.facturacion.domain.comunicacion.EstadoEnvio
import com.hospital.facturacion.domain.comunicacion.MensajeSms
import com.hospital.facturacion.domain.comunicacion.MotivoComunicacion
import com.hospital.facturacion.domain.comunicacion.PlantillasSms
import com.hospital.facturacion.domain.comunicacion.ResultadoEnvio
import com.hospital.facturacion.relojDePrueba
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NotificarPorSmsTest {
    private val bandeja = BandejaComunicacionesMemoria()
    private val plantillas = PlantillasSms { motivo, valores -> "${motivo.name} ${valores["consecutivo"]}" }
    private val falla = CanalSms { ResultadoEnvio(EstadoEnvio.FALLIDO, "twilio") }

    @Test
    fun `arma el SMS con la plantilla y deja en la bandeja tambien los envios fallidos`() {
        val cita = citaConCopago.copy(celular = Celular.de("3000000009"))
        val comunicacion = NotificarPorSms(falla, bandeja, plantillas, relojDePrueba)
            .notificar(cita, MotivoComunicacion.NOVEDAD_REGISTRADA, mapOf("consecutivo" to "PF-1"))
        val mensaje = MensajeSms(requireNotNull(Celular.de("3000000009")), "NOVEDAD_REGISTRADA PF-1")
        val esperada = Comunicacion(cita.id, MotivoComunicacion.NOVEDAD_REGISTRADA, mensaje, ResultadoEnvio(EstadoEnvio.FALLIDO, "twilio"), relojDePrueba.instant())
        assertEquals(esperada, comunicacion)
        assertEquals(listOf(esperada), ConsultarComunicaciones(bandeja).ejecutar())
    }

    @Test
    fun `sin celular registrado no envia nada`() {
        assertNull(NotificarPorSms(falla, bandeja, plantillas, relojDePrueba).notificar(citaConCopago, MotivoComunicacion.CITA_ASIGNADA, emptyMap()))
        assertEquals(emptyList(), bandeja.recientes())
    }
}
