package com.hospital.facturacion.data.archivos

import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.soporte.AlmacenSoportes
import com.hospital.facturacion.domain.soporte.DocumentoPdf
import com.hospital.facturacion.domain.soporte.TipoSoporte
import com.hospital.facturacion.domain.soporte.ValidacionPdf
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.StandardCopyOption

/**
 * Guarda cada soporte como `<cita>-<tipo>.pdf`. El nombre lo arma el servidor con valores ya validados;
 * el nombre de archivo que manda el navegador nunca se usa.
 */
class AlmacenSoportesArchivo(private val directorio: DirectorioSeguro, private val tamanoMaximoBytes: Int) : AlmacenSoportes {
    override fun guardar(cita: CitaId, tipo: TipoSoporte, pdf: DocumentoPdf) {
        val destino = requireNotNull(directorio.destino(nombre(cita, tipo))) { "Destino de soporte no permitido" }
        // Escribe aparte y mueve, para que una lectura simultánea nunca vea el archivo a medias.
        val temporal = Files.createTempFile(destino.parent, "carga-", ".tmp")
        Files.write(temporal, pdf.contenido)
        Files.move(temporal, destino, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
    }

    override fun leer(cita: CitaId, tipo: TipoSoporte): DocumentoPdf? {
        val ruta = directorio.resolver(nombre(cita, tipo)) ?: return null
        val bytes = Files.newInputStream(ruta, LinkOption.NOFOLLOW_LINKS).use { it.readNBytes(tamanoMaximoBytes + 1) }
        return when (val validacion = DocumentoPdf.validar(bytes, tamanoMaximoBytes)) {
            is ValidacionPdf.Valido -> validacion.pdf
            is ValidacionPdf.Invalido -> null
        }
    }

    override fun cargados(cita: CitaId): Set<TipoSoporte> =
        TipoSoporte.entries.filter { directorio.resolver(nombre(cita, it)) != null }.toSet()

    private fun nombre(cita: CitaId, tipo: TipoSoporte) = "${cita.valor}-${tipo.name.lowercase().replace('_', '-')}.pdf"
}
