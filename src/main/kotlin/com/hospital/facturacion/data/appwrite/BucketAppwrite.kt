package com.hospital.facturacion.data.appwrite

import java.net.URI
import java.net.http.HttpRequest
import java.time.Duration

/**
 * Un bucket de Appwrite Storage (API REST de servidor). La clave de API sale del almacén PKCS12,
 * viaja solo en la cabecera `X-Appwrite-Key` y no aparece en `toString`.
 */
class BucketAppwrite(endpoint: URI, private val proyecto: String, private val bucket: String, private val claveApi: String) {
    private val base = endpoint.toString().trimEnd('/')

    /** Petición a `/storage/buckets/{bucket}/files{ruta}` con las cabeceras de autenticación. */
    fun archivos(ruta: String = ""): HttpRequest.Builder =
        HttpRequest.newBuilder(URI.create("$base/storage/buckets/$bucket/files$ruta"))
            .timeout(Duration.ofSeconds(30))
            .header("X-Appwrite-Project", proyecto)
            .header("X-Appwrite-Key", claveApi)

    override fun toString() = "BucketAppwrite($base, $proyecto, $bucket, ****)"
}
