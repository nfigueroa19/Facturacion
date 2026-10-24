package com.hospital.facturacion.data.copago

import com.hospital.facturacion.WompiDePrueba
import com.hospital.facturacion.transaccionWompi
import org.slf4j.LoggerFactory
import java.net.http.HttpClient
import kotlin.test.Test
import kotlin.test.assertEquals

class ClienteTransaccionesWompiTest {
    private val ruta = "/v1/transactions/15113-1"

    private fun conWompi(estado: Int, respuesta: String, bloque: (ClienteTransaccionesWompi, WompiDePrueba) -> Unit) = WompiDePrueba().use { wompi ->
        wompi.responder(ruta, estado, respuesta)
        val credenciales = CredencialesWompi("pub_test_prueba", null, wompi.base)
        bloque(ClienteTransaccionesWompi(credenciales, HttpClient.newHttpClient(), LoggerFactory.getLogger("pruebas")), wompi)
    }

    @Test
    fun `consulta la transaccion sin autenticacion y lee estado, referencia, monto y moneda`() =
        conWompi(200, transaccionWompi("15113-1", "APPROVED", "cita-1_0a0a0a0a0a0a")) { cliente, wompi ->
            val esperada = TransaccionWompi("15113-1", EstadoTransaccionWompi.APPROVED, "cita-1_0a0a0a0a0a0a", 500_000, "COP")
            assertEquals(ConsultaTransaccion.Encontrada(esperada), cliente.consultar("15113-1"))
            assertEquals(listOf<String?>(null), wompi.cabeceras)
        }

    @Test
    fun `un 404 es una transaccion inexistente`() = conWompi(200, "{}") { cliente, _ ->
        assertEquals(ConsultaTransaccion.NoExiste, cliente.consultar("15113-2"))
    }

    @Test
    fun `un estado desconocido, un cuerpo ilegible o un 5xx son una falla de Wompi`() {
        conWompi(200, transaccionWompi("15113-1", "OTRO", "r")) { cliente, _ -> assertEquals(ConsultaTransaccion.Falla, cliente.consultar("15113-1")) }
        conWompi(200, "{}") { cliente, _ -> assertEquals(ConsultaTransaccion.Falla, cliente.consultar("15113-1")) }
        conWompi(503, "") { cliente, _ -> assertEquals(ConsultaTransaccion.Falla, cliente.consultar("15113-1")) }
    }
}
