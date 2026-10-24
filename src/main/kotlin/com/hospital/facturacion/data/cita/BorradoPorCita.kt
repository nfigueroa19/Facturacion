package com.hospital.facturacion.data.cita

import com.hospital.facturacion.domain.cita.CitaId

/**
 * Borra lo que un adaptador guarda de una cita: su fila, su solicitud, su copago o sus PDF. Solo lo usan los escenarios
 * de prueba para restablecerse; no es un puerto del dominio porque el negocio nunca borra una cita.
 */
fun interface BorradoPorCita {
    fun eliminar(cita: CitaId)
}
