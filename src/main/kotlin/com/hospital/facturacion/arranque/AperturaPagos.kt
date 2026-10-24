package com.hospital.facturacion.arranque

import com.hospital.facturacion.data.copago.CheckoutWompi
import com.hospital.facturacion.data.copago.ClienteContratosWompi
import com.hospital.facturacion.data.copago.ClienteTransaccionesWompi
import com.hospital.facturacion.data.copago.CredencialesWompi
import com.hospital.facturacion.data.copago.PasarelaWompi
import com.hospital.facturacion.data.secretos.FuenteSecretos
import com.hospital.facturacion.domain.copago.CheckoutPasarela
import com.hospital.facturacion.domain.copago.PasarelaPagos
import org.slf4j.Logger
import java.net.http.HttpClient
import java.security.SecureRandom

/** Secreto de integridad (`test_integrity_`) de un comercio propio. Opcional: el comercio de demostración no lo exige. */
const val ALIAS_WOMPI_INTEGRIDAD = "wompi.integridad"

/** Los dos puertos de la pasarela que usa el paciente: preparar el checkout y verificar el cobro. */
class PasarelaCopago(val checkout: CheckoutPasarela, val cobro: PasarelaPagos)

/**
 * Wompi sandbox con la llave pública de la configuración. Detiene el arranque con llaves de producción: el piloto solo
 * maneja datos ficticios y no cobra dinero.
 */
fun abrirPasarelaCopago(pagos: ConfiguracionPagos, secretos: FuenteSecretos?, http: HttpClient, aleatorio: SecureRandom, registro: Logger): PasarelaCopago {
    val integridad = secretos.textoOpcional(ALIAS_WOMPI_INTEGRIDAD)
    check(pagos.llavePublicaWompi.startsWith("pub_test_")) { "La llave pública de Wompi debe ser de pruebas (pub_test_)" }
    check(integridad == null || integridad.startsWith("test_integrity_")) { "El secreto de integridad de Wompi debe ser de pruebas" }
    registro.info("Pagos por Wompi sandbox en {}, {} firma de integridad", pagos.baseWompi, if (integridad == null) "sin" else "con")
    val credenciales = CredencialesWompi(pagos.llavePublicaWompi, integridad, pagos.baseWompi)
    val checkout = CheckoutWompi(credenciales, ClienteContratosWompi(credenciales, http, registro), aleatorio)
    return PasarelaCopago(checkout, PasarelaWompi(ClienteTransaccionesWompi(credenciales, http, registro)))
}
