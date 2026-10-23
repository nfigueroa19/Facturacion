package com.hospital.facturacion.arranque

import com.hospital.facturacion.data.postgres.crearFuenteDatos
import com.hospital.facturacion.data.postgres.migrarEsquema
import com.hospital.facturacion.data.secretos.FuenteSecretos
import com.zaxxer.hikari.HikariDataSource

/** Abre PostgreSQL con la clave guardada en el almacén PKCS12 y aplica las migraciones pendientes. */
fun abrirBaseDatos(configuracion: ConfiguracionBaseDatos, secretos: FuenteSecretos?): HikariDataSource {
    val almacen = requireNotNull(secretos) { "PostgreSQL necesita el almacén de secretos (HSLV_SECRETOS_ALMACEN)" }
    val clave = requireNotNull(almacen.leer(configuracion.aliasClave)) {
        "Falta el alias ${configuracion.aliasClave} en el almacén de secretos"
    }
    val fuente = try {
        clave.usar { crearFuenteDatos(configuracion.url, configuracion.usuario, it, configuracion.conexionesMaximas) }
    } finally {
        clave.borrar()
    }
    migrarEsquema(fuente)
    return fuente
}
