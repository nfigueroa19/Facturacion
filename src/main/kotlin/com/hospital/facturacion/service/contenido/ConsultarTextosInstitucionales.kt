package com.hospital.facturacion.service.contenido

import com.hospital.facturacion.domain.contenido.FuenteContenido
import com.hospital.facturacion.domain.contenido.LimpiadorTexto

class ConsultarTextosInstitucionales(
    private val fuente: FuenteContenido,
    private val limpiador: LimpiadorTexto,
) {
    fun ejecutar(): Map<String, String> = fuente.actual().textos.mapValues { (_, texto) -> limpiador.limpiar(texto) }
}
