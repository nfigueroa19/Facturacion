package com.hospital.facturacion.domain.soporte

import com.hospital.facturacion.domain.cita.CitaId

/** Un archivo por cita y tipo de soporte: cargar de nuevo reemplaza el anterior. */
interface AlmacenSoportes {
    fun guardar(cita: CitaId, tipo: TipoSoporte, pdf: DocumentoPdf)

    fun leer(cita: CitaId, tipo: TipoSoporte): DocumentoPdf?

    fun cargados(cita: CitaId): Set<TipoSoporte>
}
