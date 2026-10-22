package com.hospital.facturacion.domain.cita

/** Citas asignadas por el call center con su ID de acceso (RN-05). */
interface AgendaCitas {
    fun buscar(id: CitaId): Cita?

    fun buscarPorAcceso(identificacion: String, codigoAcceso: String): Cita?
}
