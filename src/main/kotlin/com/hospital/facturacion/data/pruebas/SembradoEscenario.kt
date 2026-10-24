package com.hospital.facturacion.data.pruebas

import com.hospital.facturacion.domain.cita.Cita
import com.hospital.facturacion.domain.cita.RegistroCitas
import com.hospital.facturacion.domain.copago.PagoCopago
import com.hospital.facturacion.domain.copago.PagosCopago
import com.hospital.facturacion.domain.prefactura.DatosFacturacion
import com.hospital.facturacion.domain.prefactura.EstadoPrefactura
import com.hospital.facturacion.domain.prefactura.GeneradorConsecutivo
import com.hospital.facturacion.domain.prefactura.SolicitudPrefactura
import com.hospital.facturacion.domain.prefactura.SolicitudPrefacturaRepositorio
import com.hospital.facturacion.domain.soporte.AlmacenSoportes
import com.hospital.facturacion.domain.soporte.TipoSoporte
import java.time.Clock
import java.time.LocalDate

/**
 * Escribe un escenario por los puertos del dominio: la cita fechada desde hoy y, si ya está radicada, los dos PDF,
 * el copago pagado y la solicitud llevada hasta su etapa.
 */
class SembradoEscenario(
    private val registro: RegistroCitas,
    private val solicitudes: SolicitudPrefacturaRepositorio,
    private val soportes: AlmacenSoportes,
    private val pagos: PagosCopago,
    private val consecutivo: GeneradorConsecutivo,
    private val datos: DatosFacturacion,
    private val reloj: Clock,
) {
    /** Crea la cita o la vuelve a fechar sin tocar la solicitud. */
    fun citar(escenario: EscenarioPaciente): Cita =
        escenario.cita(LocalDate.now(reloj)).also { registro.registrar(it, escenario.codigoAcceso) }

    fun sembrar(escenario: EscenarioPaciente) {
        val cita = citar(escenario)
        if (!escenario.etapa.radicada) return
        val ahora = reloj.instant()
        TipoSoporte.entries.forEach { soportes.guardar(cita.id, it, pdfDePrueba(it)) }
        cita.copago?.let { pagos.guardar(cita.id, PagoCopago("SIM-PRUEBA", it, ahora)) }
        val radicada = SolicitudPrefactura(consecutivo.siguiente(), cita.id, EstadoPrefactura.EN_REVISION, ahora, datos)
        solicitudes.guardar(escenario.etapa.aplicar(radicada, ahora))
    }
}
