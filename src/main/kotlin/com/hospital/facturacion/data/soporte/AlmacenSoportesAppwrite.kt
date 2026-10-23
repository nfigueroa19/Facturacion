package com.hospital.facturacion.data.soporte

import com.hospital.facturacion.data.appwrite.BucketAppwrite
import com.hospital.facturacion.data.appwrite.FormularioArchivo
import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.soporte.AlmacenSoportes
import com.hospital.facturacion.domain.soporte.DocumentoPdf
import com.hospital.facturacion.domain.soporte.TipoSoporte
import com.hospital.facturacion.domain.soporte.pdfAceptado
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers
import java.security.MessageDigest
import java.util.HexFormat

/**
 * PDF sin modificar en un bucket de Appwrite Storage, un archivo por cita y tipo. Appwrite no reemplaza un archivo
 * existente (responde 409), así que cargar de nuevo borra y vuelve a crear. Al leerlo se valida de nuevo.
 */
class AlmacenSoportesAppwrite(
    private val bucket: BucketAppwrite,
    private val cliente: HttpClient,
    private val tamanoMaximoBytes: Int,
) : AlmacenSoportes {
    init {
        require(tamanoMaximoBytes <= TAMANO_MAXIMO_SIN_FRAGMENTAR) { "Appwrite exige subir por partes los archivos de más de 5 MB" }
    }

    override fun guardar(cita: CitaId, tipo: TipoSoporte, pdf: DocumentoPdf) {
        val id = idArchivo(cita, tipo)
        enviar(bucket.archivos("/$id").DELETE(), BodyHandlers.discarding(), 204, 404)
        val formulario = FormularioArchivo(id, "${cita.valor}-${tipo.name.lowercase()}.pdf", "application/pdf", pdf.contenido)
        val subida = bucket.archivos().header("Content-Type", formulario.tipoContenido).POST(BodyPublishers.ofByteArray(formulario.cuerpo))
        enviar(subida, BodyHandlers.discarding(), 201)
    }

    override fun leer(cita: CitaId, tipo: TipoSoporte): DocumentoPdf? {
        val respuesta = enviar(bucket.archivos("/${idArchivo(cita, tipo)}/download").GET(), BodyHandlers.ofByteArray(), 200, 404)
        if (respuesta.statusCode() == 404) return null
        return DocumentoPdf.validar(respuesta.body(), tamanoMaximoBytes).pdfAceptado()
    }

    override fun cargados(cita: CitaId): Set<TipoSoporte> = TipoSoporte.entries.filterTo(mutableSetOf()) {
        enviar(bucket.archivos("/${idArchivo(cita, it)}").GET(), BodyHandlers.discarding(), 200, 404).statusCode() == 200
    }

    private fun <T> enviar(peticion: HttpRequest.Builder, cuerpo: HttpResponse.BodyHandler<T>, vararg esperados: Int): HttpResponse<T> =
        cliente.send(peticion.build(), cuerpo).also {
            check(it.statusCode() in esperados) { "Appwrite respondió ${it.statusCode()}; se esperaba ${esperados.joinToString()}" }
        }
}

/** Límite de Appwrite para subir un archivo en una sola petición (5 MiB). */
private const val TAMANO_MAXIMO_SIN_FRAGMENTAR = 5 * 1024 * 1024

// Appwrite admite IDs de hasta 36 caracteres [a-zA-Z0-9._-]; la cita sola puede tener 40.
private fun idArchivo(cita: CitaId, tipo: TipoSoporte): String =
    HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest("${cita.valor}/${tipo.name}".toByteArray())).take(32)
