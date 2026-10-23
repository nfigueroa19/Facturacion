package com.hospital.facturacion

import com.hospital.facturacion.data.postgres.ejecutar
import com.hospital.facturacion.data.postgres.migrarEsquema
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import java.util.concurrent.atomic.AtomicInteger
import javax.sql.DataSource

/** Base de datos vacía y aislada dentro del PostgreSQL embebido que comparten todas las pruebas. */
data class BaseDePrueba(val puerto: Int, val nombre: String, val fuente: DataSource)

/** PostgreSQL real sin Docker: zonky descarga los binarios del servidor y lo arranca una vez por JVM. */
object PostgresDePrueba {
    private val servidor: EmbeddedPostgres by lazy { EmbeddedPostgres.start() }
    private val contador = AtomicInteger()

    fun baseNueva(): BaseDePrueba {
        val nombre = "prueba_${contador.incrementAndGet()}"
        servidor.postgresDatabase.ejecutar("CREATE DATABASE $nombre")
        return BaseDePrueba(servidor.port, nombre, servidor.getDatabase("postgres", nombre))
    }

    /** Base con el esquema de `db/migracion` ya aplicado. */
    fun baseMigrada(): DataSource = baseNueva().fuente.also(::migrarEsquema)
}
