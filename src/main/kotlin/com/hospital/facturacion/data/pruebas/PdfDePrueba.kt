package com.hospital.facturacion.data.pruebas

import com.hospital.facturacion.domain.soporte.DocumentoPdf
import com.hospital.facturacion.domain.soporte.TipoSoporte
import com.hospital.facturacion.domain.soporte.pdfAceptado

/** PDF de una página con el nombre del soporte, para que el visor del facturador muestre algo legible. */
fun pdfDePrueba(tipo: TipoSoporte): DocumentoPdf {
    val texto = "Soporte de prueba: ${tipo.name}. Documento ficticio."
    val contenido = "BT /F1 14 Tf 40 120 Td ($texto) Tj ET"
    val objetos = listOf(
        "<< /Type /Catalog /Pages 2 0 R >>",
        "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
        "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 420 240] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>",
        "<< /Length ${contenido.length} >>\nstream\n$contenido\nendstream",
        "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>",
    )
    val pdf = StringBuilder("%PDF-1.4\n")
    val posiciones = objetos.mapIndexed { i, objeto ->
        pdf.length.also { pdf.append("${i + 1} 0 obj\n$objeto\nendobj\n") }
    }
    val inicioTabla = pdf.length
    pdf.append("xref\n0 ${objetos.size + 1}\n0000000000 65535 f \n")
    posiciones.forEach { pdf.append("%010d 00000 n \n".format(it)) }
    pdf.append("trailer\n<< /Size ${objetos.size + 1} /Root 1 0 R >>\nstartxref\n$inicioTabla\n%%EOF\n")
    val bytes = pdf.toString().toByteArray(Charsets.US_ASCII)
    return requireNotNull(DocumentoPdf.validar(bytes, bytes.size).pdfAceptado())
}
