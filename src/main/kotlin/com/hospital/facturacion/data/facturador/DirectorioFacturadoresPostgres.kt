package com.hospital.facturacion.data.facturador

import com.hospital.facturacion.data.postgres.consultar
import com.hospital.facturacion.data.postgres.ejecutar
import com.hospital.facturacion.data.postgres.enTransaccion
import com.hospital.facturacion.domain.facturador.DirectorioFacturadores
import com.hospital.facturacion.domain.facturador.Facturador
import javax.sql.DataSource

class DirectorioFacturadoresPostgres(private val fuente: DataSource, private val verificador: VerificadorClave) : DirectorioFacturadores {
    override fun autenticar(usuario: String, clave: String): Facturador? = fuente
        .consultar("SELECT nombre, hash_clave FROM facturador WHERE usuario = ?", usuario) { it.getString("nombre") to it.getString("hash_clave") }
        .singleOrNull()
        ?.takeIf { (_, hash) -> verificador.verifica(clave, hash) }
        ?.let { (nombre, _) -> Facturador(usuario, nombre) }
}

/** Crea o actualiza los usuarios del archivo de arranque. Los que ya no están en el archivo no se borran. */
fun sembrarFacturadores(fuente: DataSource, usuarios: List<FacturadorPruebaJson>) = fuente.enTransaccion { conexion ->
    usuarios.forEach {
        conexion.ejecutar(
            """
            INSERT INTO facturador (usuario, nombre, hash_clave) VALUES (?, ?, ?)
            ON CONFLICT (usuario) DO UPDATE SET nombre = EXCLUDED.nombre, hash_clave = EXCLUDED.hash_clave
            """,
            it.usuario, it.nombre, it.hashClave,
        )
    }
}
