package com.hospital.facturacion.arranque

import com.hospital.facturacion.data.cita.BorradoPorCita
import com.hospital.facturacion.domain.cita.AgendaCitas
import com.hospital.facturacion.domain.cita.RegistroCitas
import com.hospital.facturacion.domain.comunicacion.BandejaComunicaciones
import com.hospital.facturacion.domain.copago.PagosCopago
import com.hospital.facturacion.domain.digiturno.GeneradorTurnos
import com.hospital.facturacion.domain.facturador.DirectorioFacturadores
import com.hospital.facturacion.domain.prefactura.GeneradorConsecutivo
import com.hospital.facturacion.domain.prefactura.SolicitudPrefacturaRepositorio
import com.hospital.facturacion.domain.soporte.AlmacenSoportes

/**
 * Puertos de persistencia de un mismo destino, tipados con las interfaces del dominio: los casos de uso no saben si
 * detrás hay PostgreSQL o Appwrite. `cerrar` libera las conexiones al detener el servidor. `borrado` solo existe con
 * Appwrite: lo usan los escenarios de prueba para restablecerse.
 */
class Persistencia(
    val agenda: AgendaCitas,
    val registroCitas: RegistroCitas,
    val solicitudes: SolicitudPrefacturaRepositorio,
    val pagos: PagosCopago,
    val consecutivo: GeneradorConsecutivo,
    val facturadores: DirectorioFacturadores,
    val soportes: AlmacenSoportes,
    val comunicaciones: BandejaComunicaciones,
    val turnos: GeneradorTurnos,
    val borrado: BorradoPorCita?,
    val cerrar: () -> Unit,
)
