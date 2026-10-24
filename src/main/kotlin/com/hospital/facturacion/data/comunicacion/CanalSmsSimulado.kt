package com.hospital.facturacion.data.comunicacion

import com.hospital.facturacion.domain.comunicacion.CanalSms
import com.hospital.facturacion.domain.comunicacion.EstadoEnvio
import com.hospital.facturacion.domain.comunicacion.MensajeSms
import com.hospital.facturacion.domain.comunicacion.ResultadoEnvio

/** No envía nada: el mensaje queda solo en la bandeja y se ve en el panel de comunicaciones del personal. */
class CanalSmsSimulado : CanalSms {
    override fun enviar(mensaje: MensajeSms) = ResultadoEnvio(EstadoEnvio.SIMULADO, "simulado")
}

/**
 * Envía por el canal real solo a los celulares autorizados (los verificados en la cuenta de pruebas del proveedor);
 * el resto se simula. Evita escribirle a un desconocido cuando alguien digita un celular ficticio.
 */
class CanalSmsRestringido(
    private val autorizados: Set<String>,
    private val real: CanalSms,
    private val respaldo: CanalSms,
) : CanalSms {
    override fun enviar(mensaje: MensajeSms) =
        if (mensaje.destino.valor in autorizados) real.enviar(mensaje) else respaldo.enviar(mensaje)
}
