package com.hospital.facturacion.data.configuracion

import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path

// El Bloc de notas de Windows puede guardar UTF-8 con BOM, que el parser JSON rechaza.
inline fun <reified T> Json.leerArchivo(archivo: Path): T =
    decodeFromString<T>(Files.readString(archivo).trimStart(Char(0xFEFF)))
