package com.hospital.facturacion.data.soporte

import com.hospital.facturacion.data.appwrite.ClienteAppwrite
import com.hospital.facturacion.data.appwrite.FormularioArchivo
import com.hospital.facturacion.data.appwrite.idAppwrite
import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.soporte.AlmacenSoportes
import com.hospital.facturacion.domain.soporte.DocumentoPdf
import com.hospital.facturacion.domain.soporte.TipoSoporte
import com.hospital.facturacion.domain.soporte.pdfAceptado
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers

/**
 * PDF sin modificar en un bucket de Appwrite Storage, un archivo por cita y tipo. Appwrite no reemplaza un archivo
 * existente (responde 409), así que cargar de nuevo borra y vuelve a crear. Al leerlo se valida de nuevo.
 */
class AlmacenSoportesAppwrite(
    private val cliente: ClienteAppwrite,
    bucket: String,
    private val tamanoMaximoBytes: Int,
) : AlmacenSoportes {
    private val archivos = "/storage/buckets/$bucket/files"

    init {
        require(tamanoMaximoBytes <= TAMANO_MAXIMO_SIN_FRAGMENTAR) { "Appwrite exige subir por partes los archivos de más de 5 MB" }
    }

    override fun guardar(cita: CitaId, tipo: TipoSoporte, pdf: DocumentoPdf) {
        val id = idArchivo(cita, tipo)
        cliente.enviar("$archivos/$id", BodyHandlers.discarding(), 204, 404) { DELETE() }
        val formulario = FormularioArchivo(id, "${cita.valor}-${tipo.name.lowercase()}.pdf", "application/pdf", pdf.contenido)
        cliente.enviar(archivos, BodyHandlers.discarding(), 201) {
            header("Content-Type", formulario.tipoContenido).POST(BodyPublishers.ofByteArray(formulario.cuerpo))
        }
    }

    override fun leer(cita: CitaId, tipo: TipoSoporte): DocumentoPdf? {
        val respuesta = cliente.enviar("$archivos/${idArchivo(cita, tipo)}/download", BodyHandlers.ofByteArray(), 200, 404) { GET() }
        if (respuesta.statusCode() == 404) return null
        return DocumentoPdf.validar(respuesta.body(), tamanoMaximoBytes).pdfAceptado()
    }

    override fun cargados(cita: CitaId): Set<TipoSoporte> = TipoSoporte.entries.filterTo(mutableSetOf()) {
        cliente.enviar("$archivos/${idArchivo(cita, it)}", BodyHandlers.discarding(), 200, 404) { GET() }.statusCode() == 200
    }
}

/** Límite de Appwrite para subir un archivo en una sola petición (5 MiB). */
private const val TAMANO_MAXIMO_SIN_FRAGMENTAR = 5 * 1024 * 1024

private fun idArchivo(cita: CitaId, tipo: TipoSoporte): String = idAppwrite("${cita.valor}/${tipo.name}")
