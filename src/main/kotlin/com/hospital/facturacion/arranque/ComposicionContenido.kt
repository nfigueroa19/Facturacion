package com.hospital.facturacion.arranque

import com.hospital.facturacion.data.archivos.AlmacenImagenesArchivo
import com.hospital.facturacion.data.archivos.DirectorioSeguro
import com.hospital.facturacion.data.contenido.FuenteContenidoRecargable
import com.hospital.facturacion.data.contenido.LectorContenidoJson
import com.hospital.facturacion.data.limpieza.LimpiadorJpeg
import com.hospital.facturacion.data.limpieza.LimpiadorMetadatosPorFormato
import com.hospital.facturacion.data.limpieza.LimpiadorPng
import com.hospital.facturacion.data.limpieza.LimpiadorTextoInvisible
import com.hospital.facturacion.service.contenido.ConsultarTextosInstitucionales
import com.hospital.facturacion.service.contenido.ObtenerImagenInstitucional
import kotlinx.serialization.json.Json
import org.slf4j.Logger

class ComposicionContenido(configuracion: ConfiguracionContenido, registro: Logger) {
    private val fuente = FuenteContenidoRecargable(LectorContenidoJson(configuracion.archivo, Json), registro)
    private val almacen = AlmacenImagenesArchivo(
        DirectorioSeguro(configuracion.directorioImagenes),
        configuracion.tamanoMaximoImagenBytes,
    )
    private val limpiadorImagenes = LimpiadorMetadatosPorFormato(listOf(LimpiadorPng(), LimpiadorJpeg()))

    val textos = ConsultarTextosInstitucionales(fuente, LimpiadorTextoInvisible())
    val imagenes = ObtenerImagenInstitucional(fuente, almacen, limpiadorImagenes)
}
