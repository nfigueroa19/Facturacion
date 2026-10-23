package com.hospital.facturacion.arranque

import com.hospital.facturacion.domain.cita.AgendaCitas
import com.hospital.facturacion.domain.copago.PagosCopago
import com.hospital.facturacion.domain.facturador.DirectorioFacturadores
import com.hospital.facturacion.domain.prefactura.GeneradorConsecutivo
import com.hospital.facturacion.domain.prefactura.SolicitudPrefacturaRepositorio
import com.hospital.facturacion.domain.soporte.AlmacenSoportes

/**
 * Puertos de persistencia de un mismo destino, tipados con las interfaces del dominio: los casos de uso no saben si
 * detrás hay PostgreSQL o Appwrite. `cerrar` libera las conexiones al detener el servidor.
 */
class Persistencia(
    val agenda: AgendaCitas,
    val solicitudes: SolicitudPrefacturaRepositorio,
    val pagos: PagosCopago,
    val consecutivo: GeneradorConsecutivo,
    val facturadores: DirectorioFacturadores,
    val soportes: AlmacenSoportes,
    val cerrar: () -> Unit,
)
