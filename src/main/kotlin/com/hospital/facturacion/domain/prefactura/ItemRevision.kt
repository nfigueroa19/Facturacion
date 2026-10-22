package com.hospital.facturacion.domain.prefactura

import com.hospital.facturacion.domain.cita.Cita

/** Puntos del checklist: el facturador valida datos, soportes y copago (RN-04). */
enum class ItemRevision {
    DATOS,
    AUTORIZACION,
    ORDEN_MEDICA,
    COPAGO;

    companion object {
        fun requeridosPara(cita: Cita): Set<ItemRevision> =
            if (cita.copago == null) entries.toSet() - COPAGO else entries.toSet()
    }
}
