package com.hospital.facturacion.api.plataforma

import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.facturador.Facturador
import com.hospital.facturacion.domain.sesion.Sesiones
import com.hospital.facturacion.domain.sesion.TokenSesion
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.bearer

const val AUTENTICACION_PACIENTE = "paciente"
const val AUTENTICACION_FACTURADOR = "facturador"

/** Principal de las rutas del paciente: la sesión abre una sola cita. */
data class PacienteAutenticado(val cita: CitaId)

fun Application.instalarAutenticacion(pacientes: Sesiones<CitaId>, facturadores: Sesiones<Facturador>) {
    install(Authentication) {
        bearer(AUTENTICACION_PACIENTE) {
            authenticate { credencial -> pacientes.sujeto(TokenSesion(credencial.token))?.let(::PacienteAutenticado) }
        }
        bearer(AUTENTICACION_FACTURADOR) {
            authenticate { credencial -> facturadores.sujeto(TokenSesion(credencial.token)) }
        }
    }
}
