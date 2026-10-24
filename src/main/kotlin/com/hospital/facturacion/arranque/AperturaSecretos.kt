package com.hospital.facturacion.arranque

import com.hospital.facturacion.data.secretos.AlmacenSecretosPkcs12
import com.hospital.facturacion.data.secretos.FuenteSecretos
import com.hospital.facturacion.data.secretos.Secreto
import org.slf4j.Logger
import java.nio.file.Files
import java.nio.file.Path

const val VARIABLE_CLAVE_ARCHIVO = "HSLV_SECRETOS_CLAVE_ARCHIVO"
const val VARIABLE_CLAVE = "HSLV_SECRETOS_CLAVE"

/** Abre el almacén y detiene el arranque si falta un alias requerido. Solo registra nombres de alias. */
fun abrirSecretos(configuracion: ConfiguracionSecretos, entorno: (String) -> String?, registro: Logger): FuenteSecretos? {
    val ruta = configuracion.almacen
    if (ruta == null) {
        check(configuracion.requeridos.isEmpty()) { "Hay secretos requeridos pero falta HSLV_SECRETOS_ALMACEN" }
        registro.info("Sin almacén de secretos: no hay adaptadores que lo requieran")
        return null
    }
    val clave = claveDelAlmacen(entorno) ?: error("Falta $VARIABLE_CLAVE_ARCHIVO o $VARIABLE_CLAVE para abrir $ruta")
    val almacen = AlmacenSecretosPkcs12(ruta, clave)
    val faltantes = configuracion.requeridos - almacen.aliases()
    check(faltantes.isEmpty()) { "Faltan alias en el almacén de secretos: ${faltantes.joinToString()}" }
    registro.info("Almacén de secretos abierto: {} alias", almacen.aliases().size)
    return almacen
}

/** Secreto que un adaptador necesita para arrancar; detiene el arranque si falta el almacén o el alias. */
fun FuenteSecretos?.exigir(alias: String, adaptador: String): Secreto {
    val almacen = requireNotNull(this) { "$adaptador necesita el almacén de secretos (HSLV_SECRETOS_ALMACEN)" }
    return requireNotNull(almacen.leer(alias)) { "Falta el alias $alias en el almacén de secretos" }
}

/** Valor de texto de un alias opcional: null si no hay almacén o falta el alias. */
fun FuenteSecretos?.textoOpcional(alias: String): String? = this?.leer(alias)?.consumir { String(it).trim() }

// El archivo de clave es la forma de Docker secrets; la variable directa queda para desarrollo local.
private fun claveDelAlmacen(entorno: (String) -> String?): CharArray? =
    entorno(VARIABLE_CLAVE_ARCHIVO)?.let { Files.readString(Path.of(it)).trim().toCharArray() }
        ?: entorno(VARIABLE_CLAVE)?.toCharArray()
