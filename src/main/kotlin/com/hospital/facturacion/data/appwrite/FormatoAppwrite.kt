package com.hospital.facturacion.data.appwrite

import java.security.MessageDigest
import java.time.Instant
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.HexFormat

// Appwrite admite IDs de hasta 36 caracteres [a-zA-Z0-9._-]; una cita puede tener 40 y un usuario otros caracteres.
fun idAppwrite(clave: String): String =
    HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(clave.toByteArray())).take(32)

// Appwrite guarda las fechas con milisegundos; se recortan al escribir para leer lo mismo que se guardó.
fun Instant.fechaAppwrite(): String = truncatedTo(ChronoUnit.MILLIS).toString()

fun String.instanteAppwrite(): Instant = OffsetDateTime.parse(this).toInstant()
