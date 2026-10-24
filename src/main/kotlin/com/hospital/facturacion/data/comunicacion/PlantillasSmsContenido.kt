package com.hospital.facturacion.data.comunicacion

import com.hospital.facturacion.domain.comunicacion.MotivoComunicacion
import com.hospital.facturacion.domain.comunicacion.PlantillasSms
import com.hospital.facturacion.domain.contenido.FuenteContenido

/** Plantillas `sms.<MOTIVO>` de `config/contenido.json`: el personal administrativo las edita sin recompilar. */
class PlantillasSmsContenido(private val fuente: FuenteContenido) : PlantillasSms {
    override fun texto(motivo: MotivoComunicacion, valores: Map<String, String>): String {
        val plantilla = checkNotNull(fuente.actual().textos["sms.${motivo.name}"]) { "Falta la plantilla sms.${motivo.name}" }
        return valores.entries.fold(plantilla) { texto, (clave, valor) -> texto.replace("{$clave}", valor) }
    }
}
