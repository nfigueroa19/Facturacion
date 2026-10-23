package com.hospital.facturacion.arranque

import com.hospital.facturacion.data.appwrite.BucketAppwrite
import com.hospital.facturacion.data.secretos.FuenteSecretos
import com.hospital.facturacion.data.soporte.AlmacenSoportesAppwrite
import com.hospital.facturacion.data.soporte.AlmacenSoportesPostgres
import com.hospital.facturacion.domain.soporte.AlmacenSoportes
import java.net.URI
import java.net.http.HttpClient
import java.time.Duration
import javax.sql.DataSource

const val ALIAS_APPWRITE_ENDPOINT = "appwrite.endpoint"
const val ALIAS_APPWRITE_PROYECTO = "appwrite.proyecto"
const val ALIAS_APPWRITE_CLAVE = "appwrite.clave"

/** Dónde quedan los PDF de soporte: en la tabla `soporte` de PostgreSQL o en un bucket de Appwrite Storage. */
fun abrirSoportes(
    configuracion: ConfiguracionSoportes,
    tamanoMaximoBytes: Int,
    baseDatos: DataSource,
    secretos: FuenteSecretos?,
): AlmacenSoportes = when (configuracion.destino) {
    DestinoSoportes.POSTGRES -> AlmacenSoportesPostgres(baseDatos, tamanoMaximoBytes)
    DestinoSoportes.APPWRITE -> AlmacenSoportesAppwrite(
        abrirAppwrite(configuracion.bucketAppwrite, secretos),
        HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(),
        tamanoMaximoBytes,
    )
}

/** Endpoint, proyecto y clave de API salen del almacén PKCS12; el bucket no es secreto y va en la configuración. */
fun abrirAppwrite(bucket: String, secretos: FuenteSecretos?): BucketAppwrite {
    fun valor(alias: String) = secretos.exigir(alias, "Appwrite").consumir { String(it) }
    return BucketAppwrite(URI(valor(ALIAS_APPWRITE_ENDPOINT)), valor(ALIAS_APPWRITE_PROYECTO), bucket, valor(ALIAS_APPWRITE_CLAVE))
}
