package com.hospital.facturacion.domain.soporte

/**
 * Soporte en PDF (RN-07). Se valida por contenido, no por el nombre que trae el navegador, y se guarda
 * sin modificar: reescribirlo podría invalidar la firma digital de la EPS (P-21).
 */
class DocumentoPdf private constructor(val contenido: ByteArray) {
    companion object {
        private val FIRMA = "%PDF-".toByteArray(Charsets.US_ASCII)

        fun validar(bytes: ByteArray, tamanoMaximoBytes: Int): ValidacionPdf = when {
            bytes.isEmpty() -> ValidacionPdf.Invalido(MotivoRechazoPdf.VACIO)
            bytes.size > tamanoMaximoBytes -> ValidacionPdf.Invalido(MotivoRechazoPdf.EXCEDE_TAMANO)
            !bytes.copyOf(FIRMA.size).contentEquals(FIRMA) -> ValidacionPdf.Invalido(MotivoRechazoPdf.NO_ES_PDF)
            else -> ValidacionPdf.Valido(DocumentoPdf(bytes))
        }
    }
}

enum class MotivoRechazoPdf { VACIO, EXCEDE_TAMANO, NO_ES_PDF }

sealed interface ValidacionPdf {
    data class Valido(val pdf: DocumentoPdf) : ValidacionPdf

    data class Invalido(val motivo: MotivoRechazoPdf) : ValidacionPdf
}

/** El PDF si pasó la validación; null si fue rechazado. */
fun ValidacionPdf.pdfAceptado(): DocumentoPdf? = when (this) {
    is ValidacionPdf.Valido -> pdf
    is ValidacionPdf.Invalido -> null
}
