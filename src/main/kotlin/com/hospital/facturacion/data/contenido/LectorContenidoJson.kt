package com.hospital.facturacion.data.contenido

import kotlinx.serialization.json.Json
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime

class LectorContenidoJson(private val archivo: Path, private val json: Json) : LectorContenido {
    override fun version(): FileTime? = try {
        Files.getLastModifiedTime(archivo)
    } catch (e: IOException) {
        null
    }

    override fun leer(): LecturaContenido = try {
        // El Bloc de notas de Windows puede guardar UTF-8 con BOM, que el parser JSON rechaza.
        val texto = Files.readString(archivo).trimStart(Char(0xFEFF))
        json.decodeFromString<ContenidoJson>(texto).aDominio()
    } catch (e: IOException) {
        LecturaContenido.Invalida("No se pudo leer ${archivo.fileName} como UTF-8: ${e.message}")
    } catch (e: IllegalArgumentException) {
        LecturaContenido.Invalida("JSON inválido en ${archivo.fileName}: ${e.message}")
    }
}
