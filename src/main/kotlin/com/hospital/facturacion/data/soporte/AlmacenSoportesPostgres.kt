package com.hospital.facturacion.data.soporte

import com.hospital.facturacion.data.postgres.consultar
import com.hospital.facturacion.data.postgres.ejecutar
import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.soporte.AlmacenSoportes
import com.hospital.facturacion.domain.soporte.DocumentoPdf
import com.hospital.facturacion.domain.soporte.TipoSoporte
import com.hospital.facturacion.domain.soporte.pdfAceptado
import javax.sql.DataSource

/** PDF sin modificar en `soporte.contenido`. Al leerlo se valida de nuevo, por si alguien editó la fila. */
class AlmacenSoportesPostgres(private val fuente: DataSource, private val tamanoMaximoBytes: Int) : AlmacenSoportes {
    override fun guardar(cita: CitaId, tipo: TipoSoporte, pdf: DocumentoPdf) {
        fuente.ejecutar(
            "INSERT INTO soporte (cita, tipo, contenido) VALUES (?, ?, ?) ON CONFLICT (cita, tipo) DO UPDATE SET contenido = EXCLUDED.contenido",
            cita.valor, tipo.name, pdf.contenido,
        )
    }

    override fun leer(cita: CitaId, tipo: TipoSoporte): DocumentoPdf? {
        val bytes = fuente.consultar("SELECT contenido FROM soporte WHERE cita = ? AND tipo = ?", cita.valor, tipo.name) {
            it.getBytes("contenido")
        }.singleOrNull() ?: return null
        return DocumentoPdf.validar(bytes, tamanoMaximoBytes).pdfAceptado()
    }

    override fun cargados(cita: CitaId): Set<TipoSoporte> =
        fuente.consultar("SELECT tipo FROM soporte WHERE cita = ?", cita.valor) { TipoSoporte.valueOf(it.getString("tipo")) }.toSet()
}
