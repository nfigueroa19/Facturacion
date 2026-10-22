package com.hospital.facturacion

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.zip.CRC32
import javax.imageio.ImageIO

object ImagenesDePrueba {
    fun png(): ByteArray = codificar("png")

    fun jpeg(): ByteArray = codificar("jpg")

    fun chunkPng(tipo: String, datos: ByteArray): ByteArray {
        val tipoBytes = tipo.toByteArray(Charsets.ISO_8859_1)
        val crc = CRC32().apply { update(tipoBytes); update(datos) }.value.toInt()
        return ByteBuffer.allocate(12 + datos.size).putInt(datos.size).put(tipoBytes).put(datos).putInt(crc).array()
    }

    fun segmentoJpeg(marcador: Int, datos: ByteArray): ByteArray = ByteBuffer.allocate(4 + datos.size)
        .put(0xFF.toByte()).put(marcador.toByte()).putShort((datos.size + 2).toShort()).put(datos).array()

    fun decodificable(bytes: ByteArray): Boolean = ImageIO.read(bytes.inputStream())?.width == 4

    private fun codificar(formato: String): ByteArray {
        val imagen = BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB).apply { setRGB(1, 1, 0x7AB51D) }
        return ByteArrayOutputStream().also { ImageIO.write(imagen, formato, it) }.toByteArray()
    }
}

fun ByteArray.contiene(texto: String): Boolean = String(this, Charsets.ISO_8859_1).contains(texto)
