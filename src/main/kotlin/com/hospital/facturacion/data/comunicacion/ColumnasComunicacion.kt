package com.hospital.facturacion.data.comunicacion

import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.comunicacion.Celular
import com.hospital.facturacion.domain.comunicacion.Comunicacion
import com.hospital.facturacion.domain.comunicacion.EstadoEnvio
import com.hospital.facturacion.domain.comunicacion.MensajeSms
import com.hospital.facturacion.domain.comunicacion.MotivoComunicacion
import com.hospital.facturacion.domain.comunicacion.ResultadoEnvio
import java.time.Instant

/** Columnas de una comunicación, comunes a PostgreSQL y Appwrite. `fecha` en ISO 8601. */
data class ColumnasComunicacion(
    val cita: String,
    val motivo: MotivoComunicacion,
    val destino: String,
    val texto: String,
    val estado: EstadoEnvio,
    val canal: String,
    val fecha: Instant,
)

fun Comunicacion.columnas() =
    ColumnasComunicacion(cita.valor, motivo, mensaje.destino.valor, mensaje.texto, envio.estado, envio.canal, fecha)

fun ColumnasComunicacion.comunicacion() = Comunicacion(
    cita = requireNotNull(CitaId.de(cita)) { "Cita inválida en una comunicación" },
    motivo = motivo,
    mensaje = MensajeSms(requireNotNull(Celular.de(destino)) { "Celular inválido en una comunicación" }, texto),
    envio = ResultadoEnvio(estado, canal),
    fecha = fecha,
)
