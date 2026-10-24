package com.hospital.facturacion.domain.comunicacion

/** Proveedor de SMS. Un fallo del proveedor se devuelve como FALLIDO; nunca detiene el caso de uso. */
fun interface CanalSms {
    fun enviar(mensaje: MensajeSms): ResultadoEnvio
}

/** Registro de lo que se le comunicó a cada paciente, para el panel de auditoría del personal. */
interface BandejaComunicaciones {
    fun registrar(comunicacion: Comunicacion)

    /** De la más reciente a la más antigua. */
    fun recientes(): List<Comunicacion>
}

/** Textos de los SMS, editables sin recompilar. `valores` reemplaza las marcas `{nombre}` de la plantilla. */
fun interface PlantillasSms {
    fun texto(motivo: MotivoComunicacion, valores: Map<String, String>): String
}
