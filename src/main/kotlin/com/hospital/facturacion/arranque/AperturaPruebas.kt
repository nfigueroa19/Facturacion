package com.hospital.facturacion.arranque

import com.hospital.facturacion.api.pruebas.CasosPruebas
import com.hospital.facturacion.data.pruebas.CatalogoPruebas
import com.hospital.facturacion.data.pruebas.EscenariosPrueba
import com.hospital.facturacion.data.pruebas.SembradoEscenario
import com.hospital.facturacion.domain.prefactura.DatosFacturacion
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import java.time.Clock

/** Siembra los escenarios que faltan y arma las rutas `/api/dev`. Detiene el arranque si el destino no permite restablecerlos. */
fun abrirPruebas(catalogo: CatalogoPruebas, persistencia: Persistencia, reloj: Clock, registro: Logger): CasosPruebas {
    val borrado = requireNotNull(persistencia.borrado) { "El catálogo de pruebas necesita HSLV_PERSISTENCIA=appwrite" }
    val sembrado = with(persistencia) {
        SembradoEscenario(registroCitas, solicitudes, soportes, pagos, consecutivo, DatosFacturacion(catalogo.datosFormulario), reloj)
    }
    val escenarios = EscenariosPrueba(catalogo, persistencia.agenda, borrado, sembrado).also { it.preparar() }
    registro.info("Escenarios de prueba listos: {} pacientes y {} usuarios del personal", catalogo.pacientes.size, catalogo.personal.size)
    return CasosPruebas(Json.encodeToString(CatalogoPruebas.serializer(), catalogo), escenarios::restablecer)
}
