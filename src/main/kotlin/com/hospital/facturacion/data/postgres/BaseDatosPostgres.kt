package com.hospital.facturacion.data.postgres

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import javax.sql.DataSource

/** Pool de conexiones. La clave llega del almacén de secretos; Hikari solo la acepta como `String`. */
fun crearFuenteDatos(url: String, usuario: String, clave: CharArray, conexionesMaximas: Int): HikariDataSource =
    HikariDataSource(
        HikariConfig().apply {
            jdbcUrl = url
            username = usuario
            password = String(clave)
            maximumPoolSize = conexionesMaximas
            poolName = "prefactura"
        },
    )

/** Aplica las migraciones de `db/migracion` que falten. Falla el arranque si el esquema no coincide. */
fun migrarEsquema(fuente: DataSource) {
    Flyway.configure()
        .dataSource(fuente)
        .locations("classpath:db/migracion")
        .load()
        .migrate()
}
