package com.hospital.facturacion.data.contenido

import com.hospital.facturacion.domain.contenido.ContenidoInstitucional
import com.hospital.facturacion.domain.contenido.FuenteContenido
import org.slf4j.Logger
import java.nio.file.attribute.FileTime

/**
 * Relee el archivo cuando cambia su fecha de modificación. Si la edición deja el archivo inválido,
 * conserva la última versión válida y registra el motivo, para que un error de edición no tumbe la web.
 */
class FuenteContenidoRecargable(
    private val lector: LectorContenido,
    private val registro: Logger,
) : FuenteContenido {
    @Volatile private var version: FileTime? = lector.version()
    @Volatile private var vigente: ContenidoInstitucional = when (val lectura = lector.leer()) {
        is LecturaContenido.Valida -> lectura.contenido
        is LecturaContenido.Invalida -> error("Contenido institucional inválido al arrancar: ${lectura.motivo}")
    }

    override fun actual(): ContenidoInstitucional {
        val versionActual = lector.version()
        if (versionActual != version) recargar(versionActual)
        return vigente
    }

    @Synchronized
    private fun recargar(versionActual: FileTime?) {
        if (versionActual == version) return
        version = versionActual
        when (val lectura = lector.leer()) {
            is LecturaContenido.Valida -> vigente = lectura.contenido
            is LecturaContenido.Invalida -> registro.warn("Se conserva el contenido anterior. {}", lectura.motivo)
        }
    }
}
