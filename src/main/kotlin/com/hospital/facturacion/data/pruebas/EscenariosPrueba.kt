package com.hospital.facturacion.data.pruebas

import com.hospital.facturacion.data.cita.BorradoPorCita
import com.hospital.facturacion.domain.cita.AgendaCitas

/**
 * Escenarios del catálogo de pruebas. Al arrancar siembra los que faltan y vuelve a fechar los demás, sin borrar lo que
 * hicieron los probadores. [restablecer] borra las citas de todas las identificaciones del catálogo, incluidas las del
 * call center, y siembra de nuevo cada escenario en su etapa inicial.
 */
class EscenariosPrueba(
    private val catalogo: CatalogoPruebas,
    private val agenda: AgendaCitas,
    private val borrado: BorradoPorCita,
    private val sembrado: SembradoEscenario,
) {
    fun preparar() = catalogo.pacientes.forEach {
        if (agenda.buscar(it.citaId) == null) sembrado.sembrar(it) else sembrado.citar(it)
    }

    fun restablecer(): Int {
        val identificaciones = catalogo.pacientes.map { it.identificacion } + catalogo.callcenter.map { it.identificacion }
        identificaciones.flatMap(agenda::buscarPorIdentificacion).forEach { borrado.eliminar(it.id) }
        catalogo.pacientes.forEach(sembrado::sembrar)
        return catalogo.pacientes.size
    }
}
