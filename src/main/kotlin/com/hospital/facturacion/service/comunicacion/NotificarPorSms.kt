package com.hospital.facturacion.service.comunicacion

import com.hospital.facturacion.domain.cita.Cita
import com.hospital.facturacion.domain.comunicacion.BandejaComunicaciones
import com.hospital.facturacion.domain.comunicacion.CanalSms
import com.hospital.facturacion.domain.comunicacion.Comunicacion
import com.hospital.facturacion.domain.comunicacion.MensajeSms
import com.hospital.facturacion.domain.comunicacion.MotivoComunicacion
import com.hospital.facturacion.domain.comunicacion.NotificadorPaciente
import com.hospital.facturacion.domain.comunicacion.PlantillasSms
import java.time.Clock

/** Arma el SMS con la plantilla, lo envía y deja el resultado en la bandeja, aunque el envío falle o sea simulado. */
class NotificarPorSms(
    private val canal: CanalSms,
    private val bandeja: BandejaComunicaciones,
    private val plantillas: PlantillasSms,
    private val reloj: Clock,
) : NotificadorPaciente {
    override fun notificar(cita: Cita, motivo: MotivoComunicacion, valores: Map<String, String>): Comunicacion? {
        val destino = cita.celular ?: return null
        val mensaje = MensajeSms(destino, plantillas.texto(motivo, valores))
        return Comunicacion(cita.id, motivo, mensaje, canal.enviar(mensaje), reloj.instant()).also(bandeja::registrar)
    }
}
