package com.hospital.facturacion.data

import com.hospital.facturacion.AppwriteDePrueba
import com.hospital.facturacion.PostgresDePrueba
import com.hospital.facturacion.citaConCopago
import com.hospital.facturacion.data.cita.GeneradorAccesoAleatorio
import com.hospital.facturacion.data.copago.PasarelaPagosSimulada
import com.hospital.facturacion.data.digiturno.GeneradorTurnosAppwrite
import com.hospital.facturacion.data.digiturno.GeneradorTurnosPostgres
import com.hospital.facturacion.data.digiturno.prepararTurnos
import com.hospital.facturacion.data.prefactura.FilaConsecutivo
import com.hospital.facturacion.domain.cita.Copago
import com.hospital.facturacion.domain.copago.MotivoRechazoPago
import com.hospital.facturacion.domain.copago.ResultadoPasarela
import com.hospital.facturacion.domain.copago.TokenPago
import com.hospital.facturacion.domain.digiturno.DestinoTurno
import com.hospital.facturacion.domain.digiturno.GeneradorTurnos
import java.security.SecureRandom
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PasarelaPagosSimuladaTest {
    @Test
    fun `la pasarela de pruebas responde segun el token de la tarjeta de prueba`() {
        val pasarela = PasarelaPagosSimulada(SecureRandom())
        fun cobrar(token: String) = pasarela.cobrar(citaConCopago, Copago(5000), requireNotNull(TokenPago.de(token)))
        assertTrue(assertIs<ResultadoPasarela.Aprobado>(cobrar(PasarelaPagosSimulada.TOKEN_APROBADA)).referencia.startsWith("SIM-"))
        assertEquals(ResultadoPasarela.Rechazado(MotivoRechazoPago.RECHAZADA), cobrar(PasarelaPagosSimulada.TOKEN_RECHAZADA))
        assertEquals(ResultadoPasarela.Rechazado(MotivoRechazoPago.FONDOS_INSUFICIENTES), cobrar(PasarelaPagosSimulada.TOKEN_FONDOS_INSUFICIENTES))
        assertEquals(ResultadoPasarela.Rechazado(MotivoRechazoPago.ERROR_PASARELA), cobrar(PasarelaPagosSimulada.TOKEN_ERROR))
        assertEquals(ResultadoPasarela.Rechazado(MotivoRechazoPago.MEDIO_INVALIDO), cobrar("tok_otro"))
        assertEquals(null, TokenPago.de("4242 4242 4242 4242"))
    }
}
