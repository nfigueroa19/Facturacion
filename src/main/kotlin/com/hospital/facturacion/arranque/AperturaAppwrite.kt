package com.hospital.facturacion.arranque

import com.hospital.facturacion.data.appwrite.ClienteAppwrite
import com.hospital.facturacion.data.appwrite.TablaAppwrite
import com.hospital.facturacion.data.cita.AgendaCitasAppwrite
import com.hospital.facturacion.data.cita.FilaCita
import com.hospital.facturacion.data.cita.sembrarCitas
import com.hospital.facturacion.data.copago.FilaPago
import com.hospital.facturacion.data.copago.PagosCopagoAppwrite
import com.hospital.facturacion.data.facturador.ClavePbkdf2
import com.hospital.facturacion.data.facturador.DirectorioFacturadoresAppwrite
import com.hospital.facturacion.data.facturador.FilaUsuario
import com.hospital.facturacion.data.facturador.sembrarFacturadores
import com.hospital.facturacion.data.prefactura.FilaConsecutivo
import com.hospital.facturacion.data.prefactura.FilaNovedad
import com.hospital.facturacion.data.prefactura.FilaSolicitud
import com.hospital.facturacion.data.prefactura.GeneradorConsecutivoAppwrite
import com.hospital.facturacion.data.prefactura.SolicitudPrefacturaRepositorioAppwrite
import com.hospital.facturacion.data.prefactura.prepararConsecutivo
import com.hospital.facturacion.data.secretos.FuenteSecretos
import com.hospital.facturacion.data.soporte.AlmacenSoportesAppwrite
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.time.Duration

const val ALIAS_APPWRITE_ENDPOINT = "appwrite.endpoint"
const val ALIAS_APPWRITE_PROYECTO = "appwrite.proyecto"
const val ALIAS_APPWRITE_CLAVE = "appwrite.clave"

/** Tablas de TablesDB y bucket de Storage de un proyecto de Appwrite. Siembra los datos de prueba al arrancar. */
fun persistenciaAppwrite(
    configuracion: ConfiguracionPersistencia,
    secretos: FuenteSecretos?,
    datos: DatosPrueba,
    tamanoMaximoSoporteBytes: Int,
): Persistencia {
    val http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build()
    val cliente = abrirClienteAppwrite(secretos, http)
    fun <F> tabla(nombre: String, forma: KSerializer<F>) = TablaAppwrite(cliente, configuracion.baseDatosAppwrite, nombre, forma)
    val citas = tabla("citas", FilaCita.serializer()).also { sembrarCitas(it, datos.citas) }
    val usuarios = tabla("usuarios", FilaUsuario.serializer()).also { sembrarFacturadores(it, datos.facturadores) }
    val consecutivos = tabla("consecutivos", FilaConsecutivo.serializer()).also(::prepararConsecutivo)
    return Persistencia(
        agenda = AgendaCitasAppwrite(citas),
        solicitudes = SolicitudPrefacturaRepositorioAppwrite(tabla("solicitudes", FilaSolicitud.serializer()), tabla("novedades", FilaNovedad.serializer()), Json),
        pagos = PagosCopagoAppwrite(tabla("pagos_copago", FilaPago.serializer())),
        consecutivo = GeneradorConsecutivoAppwrite(consecutivos),
        facturadores = DirectorioFacturadoresAppwrite(usuarios, ClavePbkdf2()),
        soportes = AlmacenSoportesAppwrite(cliente, configuracion.bucketAppwrite, tamanoMaximoSoporteBytes),
        cerrar = http::close,
    )
}

/** Endpoint, proyecto y clave de API salen del almacén PKCS12; la base y el bucket no son secretos y van en la configuración. */
fun abrirClienteAppwrite(secretos: FuenteSecretos?, http: HttpClient): ClienteAppwrite {
    fun valor(alias: String) = secretos.exigir(alias, "Appwrite").consumir { String(it) }
    return ClienteAppwrite(URI(valor(ALIAS_APPWRITE_ENDPOINT)), valor(ALIAS_APPWRITE_PROYECTO), valor(ALIAS_APPWRITE_CLAVE), http)
}
