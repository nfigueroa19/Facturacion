package com.hospital.facturacion.arranque

import com.hospital.facturacion.data.cita.AgendaCitasMemoria
import com.hospital.facturacion.data.cita.CitaRegistrada
import com.hospital.facturacion.data.cita.leerCitasPrueba
import com.hospital.facturacion.data.comunicacion.BandejaComunicacionesPostgres
import com.hospital.facturacion.data.copago.PagosCopagoPostgres
import com.hospital.facturacion.data.digiturno.GeneradorTurnosPostgres
import com.hospital.facturacion.data.facturador.ClavePbkdf2
import com.hospital.facturacion.data.facturador.DirectorioFacturadoresPostgres
import com.hospital.facturacion.data.facturador.FacturadorPruebaJson
import com.hospital.facturacion.data.facturador.leerFacturadoresPrueba
import com.hospital.facturacion.data.facturador.sembrarFacturadores
import com.hospital.facturacion.data.prefactura.GeneradorConsecutivoPostgres
import com.hospital.facturacion.data.prefactura.SolicitudPrefacturaRepositorioPostgres
import com.hospital.facturacion.data.secretos.FuenteSecretos
import com.hospital.facturacion.data.soporte.AlmacenSoportesPostgres
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import java.time.Clock

/** Citas y facturadores ficticios del piloto, que se siembran en el destino al arrancar. */
data class DatosPrueba(val citas: List<CitaRegistrada>, val facturadores: List<FacturadorPruebaJson>)

/** Único punto que elige el destino de persistencia; cambiarlo no toca casos de uso ni rutas. */
/** `personalPruebas` son los usuarios del catálogo de pruebas, que se siembran junto con los del archivo de facturadores. */
fun abrirPersistencia(
    configuracion: ConfiguracionAplicacion,
    secretos: FuenteSecretos?,
    reloj: Clock,
    personalPruebas: List<FacturadorPruebaJson>,
    registro: Logger,
): Persistencia {
    val solicitudes = configuracion.solicitudes
    val datos = DatosPrueba(
        leerCitasPrueba(solicitudes.citasPrueba, Json, reloj),
        leerFacturadoresPrueba(solicitudes.facturadoresPrueba, Json) + personalPruebas,
    )
    return when (configuracion.persistencia.destino) {
        DestinoPersistencia.POSTGRES -> persistenciaPostgres(configuracion.baseDatos, secretos, datos, solicitudes.tamanoMaximoSoporteBytes)
        DestinoPersistencia.APPWRITE -> persistenciaAppwrite(configuracion.persistencia, secretos, datos, solicitudes.tamanoMaximoSoporteBytes, registro)
    }
}

// Con PostgreSQL la agenda sigue en memoria: el esquema no tiene tabla de citas y las del call center se pierden al reiniciar.
private fun persistenciaPostgres(
    configuracion: ConfiguracionBaseDatos,
    secretos: FuenteSecretos?,
    datos: DatosPrueba,
    tamanoMaximoSoporteBytes: Int,
): Persistencia {
    val base = abrirBaseDatos(configuracion, secretos)
    sembrarFacturadores(base, datos.facturadores)
    val agenda = AgendaCitasMemoria(datos.citas)
    return Persistencia(
        agenda = agenda,
        registroCitas = agenda,
        solicitudes = SolicitudPrefacturaRepositorioPostgres(base, Json),
        pagos = PagosCopagoPostgres(base),
        consecutivo = GeneradorConsecutivoPostgres(base),
        facturadores = DirectorioFacturadoresPostgres(base, ClavePbkdf2()),
        soportes = AlmacenSoportesPostgres(base, tamanoMaximoSoporteBytes),
        comunicaciones = BandejaComunicacionesPostgres(base),
        turnos = GeneradorTurnosPostgres(base),
        borrado = null,
        cerrar = base::close,
    )
}
