package com.hospital.facturacion.data.copago

import com.hospital.facturacion.domain.cita.Cita
import com.hospital.facturacion.domain.cita.Copago
import com.hospital.facturacion.domain.copago.PasarelaPagos
import com.hospital.facturacion.domain.copago.ResultadoPasarela
import java.security.SecureRandom
import java.util.HexFormat

/** Aprueba todo pago sin cobrar dinero. Ocupa el lugar del adaptador de la pasarela real (P-14). */
class PasarelaPagosSimulada(private val aleatorio: SecureRandom) : PasarelaPagos {
    override fun cobrar(cita: Cita, copago: Copago): ResultadoPasarela {
        val referencia = ByteArray(4).also(aleatorio::nextBytes)
        return ResultadoPasarela.Aprobado("SIM-" + HexFormat.of().withUpperCase().formatHex(referencia))
    }
}
