package com.hospital.facturacion.arranque

import com.hospital.facturacion.data.postgres.crearFuenteDatos
import com.hospital.facturacion.data.postgres.migrarEsquema
import com.hospital.facturacion.data.secretos.FuenteSecretos
import com.zaxxer.hikari.HikariDataSource

/** Abre PostgreSQL con la clave guardada en el almacén PKCS12 y aplica las migraciones pendientes. */
fun abrirBaseDatos(configuracion: ConfiguracionBaseDatos, secretos: FuenteSecretos?): HikariDataSource {
    val fuente = secretos.exigir(configuracion.aliasClave, "PostgreSQL").consumir {
        crearFuenteDatos(configuracion.url, configuracion.usuario, it, configuracion.conexionesMaximas)
    }
    migrarEsquema(fuente)
    return fuente
}
