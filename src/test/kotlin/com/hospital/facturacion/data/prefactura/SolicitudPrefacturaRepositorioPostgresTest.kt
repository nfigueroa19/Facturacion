package com.hospital.facturacion.data.prefactura

import com.hospital.facturacion.PostgresDePrueba
import kotlinx.serialization.json.Json

class SolicitudPrefacturaRepositorioPostgresTest : RepositorioSolicitudesContrato() {
    override val repositorio = SolicitudPrefacturaRepositorioPostgres(PostgresDePrueba.baseMigrada(), Json)
}
