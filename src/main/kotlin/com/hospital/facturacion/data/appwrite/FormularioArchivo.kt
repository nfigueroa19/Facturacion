package com.hospital.facturacion.data.appwrite

import java.io.ByteArrayOutputStream
import java.util.UUID

/** Cuerpo `multipart/form-data` de `POST /storage/buckets/{bucket}/files`: el `fileId` y el archivo sin modificar. */
class FormularioArchivo(idArchivo: String, nombre: String, tipoArchivo: String, contenido: ByteArray) {
    private val limite = "prefactura-" + UUID.randomUUID()

    val tipoContenido = "multipart/form-data; boundary=$limite"

    val cuerpo: ByteArray = ByteArrayOutputStream().apply {
        writeBytes("--$limite\r\nContent-Disposition: form-data; name=\"fileId\"\r\n\r\n$idArchivo\r\n".toByteArray())
        writeBytes("--$limite\r\nContent-Disposition: form-data; name=\"file\"; filename=\"$nombre\"\r\n".toByteArray())
        writeBytes("Content-Type: $tipoArchivo\r\n\r\n".toByteArray())
        writeBytes(contenido)
        writeBytes("\r\n--$limite--\r\n".toByteArray())
    }.toByteArray()
}
