package com.hospital.facturacion.domain.cita

/** Citas asignadas por el call center con su ID de acceso (RN-05). */
interface AgendaCitas {
    fun buscar(id: CitaId): Cita?

    fun buscarPorAcceso(identificacion: String, codigoAcceso: String): Cita?

    /** El digiturno identifica al paciente solo con su número de identificación. */
    fun buscarPorIdentificacion(identificacion: String): List<Cita>
}

/** El call center agrega citas a la agenda con el ID de acceso que recibe el paciente. */
fun interface RegistroCitas {
    fun registrar(cita: Cita, codigoAcceso: String)
}

/** Identificadores aleatorios de la cita y del ID de acceso que viaja por SMS. */
interface GeneradorAcceso {
    fun citaId(): CitaId

    fun codigoAcceso(): String
}
