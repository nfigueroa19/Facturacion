package com.hospital.facturacion.data.postgres

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.sql.DataSource

// Sentencias siempre parametrizadas: ningún valor del paciente se concatena en el SQL.

fun <T> DataSource.enTransaccion(bloque: (Connection) -> T): T = connection.use { conexion ->
    conexion.autoCommit = false
    try {
        bloque(conexion).also { conexion.commit() }
    } catch (causa: Throwable) {
        conexion.rollback()
        throw causa
    }
}

fun Connection.ejecutar(sql: String, vararg parametros: Any?): Int = preparar(sql, parametros).use { it.executeUpdate() }

fun <T> Connection.consultar(sql: String, vararg parametros: Any?, fila: (ResultSet) -> T): List<T> =
    preparar(sql, parametros).use { sentencia ->
        sentencia.executeQuery().use { resultado -> buildList { while (resultado.next()) add(fila(resultado)) } }
    }

fun DataSource.ejecutar(sql: String, vararg parametros: Any?): Int = connection.use { it.ejecutar(sql, *parametros) }

fun <T> DataSource.consultar(sql: String, vararg parametros: Any?, fila: (ResultSet) -> T): List<T> =
    connection.use { it.consultar(sql, *parametros, fila = fila) }

// pgjdbc no acepta Instant en setObject; TIMESTAMPTZ viaja como OffsetDateTime en UTC.
fun Instant.utc(): OffsetDateTime = atOffset(ZoneOffset.UTC)

fun ResultSet.instante(columna: String): Instant? = getObject(columna, OffsetDateTime::class.java)?.toInstant()

private fun Connection.preparar(sql: String, parametros: Array<out Any?>): PreparedStatement =
    prepareStatement(sql).apply { parametros.forEachIndexed { i, valor -> setObject(i + 1, valor) } }
