package com.hospital.facturacion.arranque

import com.hospital.facturacion.data.copago.CheckoutWompi
import com.hospital.facturacion.data.copago.ClienteTransaccionesWompi
import com.hospital.facturacion.data.copago.CredencialesWompi
import com.hospital.facturacion.data.copago.PasarelaPagosSimulada
import com.hospital.facturacion.data.copago.PasarelaWompi
import com.hospital.facturacion.data.secretos.FuenteSecretos
import com.hospital.facturacion.domain.copago.CheckoutPasarela
import com.hospital.facturacion.domain.copago.PasarelaPagos
import org.slf4j.Logger
import java.net.http.HttpClient
import java.security.SecureRandom

/** Llaves de pruebas del panel de Wompi: pública (`pub_test_`), privada (`prv_test_`) y secreto de integridad. */
const val ALIAS_WOMPI_PUBLICA = "wompi.publica"
const val ALIAS_WOMPI_PRIVADA = "wompi.privada"
const val ALIAS_WOMPI_INTEGRIDAD = "wompi.integridad"

/** Los dos puertos de la pasarela que usa el paciente: preparar el checkout y verificar el cobro. */
class PasarelaCopago(val checkout: CheckoutPasarela, val cobro: PasarelaPagos)

/**
 * Wompi sandbox si el almacén PKCS12 trae sus tres llaves; si no, la pasarela simulada. Detiene el arranque con llaves
 * de producción: el piloto solo maneja datos ficticios y no cobra dinero.
 */
fun abrirPasarelaCopago(secretos: FuenteSecretos?, http: HttpClient, aleatorio: SecureRandom, registro: Logger): PasarelaCopago {
    val publica = secretos.textoOpcional(ALIAS_WOMPI_PUBLICA)
    val privada = secretos.textoOpcional(ALIAS_WOMPI_PRIVADA)
    val integridad = secretos.textoOpcional(ALIAS_WOMPI_INTEGRIDAD)
    if (publica == null || privada == null || integridad == null) {
        registro.info("Pagos simulados: el almacén no tiene las tres llaves de Wompi")
        return PasarelaPagosSimulada(aleatorio).let { PasarelaCopago(it, it) }
    }
    check(publica.startsWith("pub_test_") && privada.startsWith("prv_test_") && integridad.startsWith("test_integrity_")) {
        "Las llaves de Wompi deben ser de pruebas: pub_test_, prv_test_ y test_integrity_"
    }
    registro.info("Pagos por Wompi sandbox")
    val credenciales = CredencialesWompi(publica, privada, integridad)
    return PasarelaCopago(CheckoutWompi(credenciales, aleatorio), PasarelaWompi(ClienteTransaccionesWompi(credenciales, http, registro)))
}
