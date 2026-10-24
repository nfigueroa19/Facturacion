package com.hospital.facturacion.data.cita

import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.cita.GeneradorAcceso
import java.security.SecureRandom
import java.util.HexFormat

/**
 * ID de cita `cita-` + 12 hexadecimales y ID de acceso de 8 caracteres sin letras que se confundan al dictarlas
 * (I, O, 0, 1). El formato del ID real del call center no está documentado (P-09).
 */
class GeneradorAccesoAleatorio(private val aleatorio: SecureRandom) : GeneradorAcceso {
    override fun citaId(): CitaId =
        requireNotNull(CitaId.de("cita-" + HexFormat.of().formatHex(ByteArray(6).also(aleatorio::nextBytes))))

    override fun codigoAcceso(): String = String(CharArray(LARGO_CODIGO) { ALFABETO[aleatorio.nextInt(ALFABETO.length)] })

    private companion object {
        const val LARGO_CODIGO = 8
        const val ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    }
}
