package com.hospital.facturacion.domain.soporte

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DocumentoPdfTest {
    private fun motivo(bytes: ByteArray, maximo: Int = 64) =
        (DocumentoPdf.validar(bytes, maximo) as? ValidacionPdf.Invalido)?.motivo

    @Test
    fun `acepta un PDF dentro del limite sin modificar sus bytes`() {
        val bytes = "%PDF-1.7\n%firma".toByteArray()
        val pdf = assertIs<ValidacionPdf.Valido>(DocumentoPdf.validar(bytes, 64)).pdf
        assertEquals(bytes.toList(), pdf.contenido.toList())
    }

    @Test
    fun `rechaza archivos vacios, mas grandes que el limite o que no empiezan con la firma PDF`() {
        assertEquals(MotivoRechazoPdf.VACIO, motivo(ByteArray(0)))
        assertEquals(MotivoRechazoPdf.EXCEDE_TAMANO, motivo("%PDF-".toByteArray() + ByteArray(64)))
        assertEquals(MotivoRechazoPdf.NO_ES_PDF, motivo(byteArrayOf(0x89.toByte(), 'P'.code.toByte(), 'N'.code.toByte(), 'G'.code.toByte())))
        assertEquals(MotivoRechazoPdf.NO_ES_PDF, motivo("%PD".toByteArray()))
        assertEquals(MotivoRechazoPdf.NO_ES_PDF, motivo("<html>%PDF-".toByteArray()))
    }
}
