package com.hospital.facturacion.data.pruebas

import com.hospital.facturacion.data.configuracion.leerArchivo
import com.hospital.facturacion.data.facturador.FacturadorPruebaJson
import com.hospital.facturacion.data.facturador.GeneradorHashPbkdf2
import com.hospital.facturacion.domain.cita.Cita
import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.cita.Copago
import com.hospital.facturacion.domain.cita.Regimen
import com.hospital.facturacion.domain.comunicacion.Celular
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.nio.file.Path
import java.time.LocalDate

/**
 * Forma de `config/usuarios-prueba.json`: usuarios del personal, pacientes con un escenario cada uno e identificaciones
 * libres para el call center. Todo es ficticio; las claves van en texto plano porque la web las muestra a los probadores.
 */
@Serializable
data class CatalogoPruebas(
    val personal: List<UsuarioPrueba>,
    val pacientes: List<EscenarioPaciente>,
    val callcenter: List<PacienteCallCenter>,
    val datosFormulario: Map<String, String>,
)

@Serializable
data class UsuarioPrueba(val usuario: String, val clave: String, val nombre: String, val modulo: String, val uso: String)

@Serializable
data class PacienteCallCenter(val identificacion: String, val celular: String, val pasos: List<String>)

@Serializable
data class EscenarioPaciente(
    val escenario: String,
    val identificacion: String,
    val codigoAcceso: String,
    val diasDesdeHoy: Long,
    val regimen: Regimen,
    val copagoPesos: Long? = null,
    val celular: String,
    val etapa: EtapaEscenario,
    val pasos: List<String>,
) {
    /** El ID de la cita sale de la identificación: el mismo escenario siempre ocupa la misma fila. */
    val citaId: CitaId get() = requireNotNull(CitaId.de("escenario-" + identificacion.lowercase())) { "Identificación no permitida: $identificacion" }

    fun cita(hoy: LocalDate) = Cita(
        id = citaId,
        identificacionPaciente = identificacion,
        fecha = hoy.plusDays(diasDesdeHoy),
        medico = "Médico de prueba",
        especialidad = "Especialidad de prueba",
        regimen = regimen,
        copago = copagoPesos?.let(::Copago),
        celular = requireNotNull(Celular.de(celular)) { "Celular no permitido en el escenario $escenario" },
    )
}

fun leerCatalogoPruebas(archivo: Path, json: Json): CatalogoPruebas = json.leerArchivo(archivo)

/** Usuarios del personal con el hash de su clave, listos para sembrar en el directorio de facturadores. */
fun CatalogoPruebas.facturadores(generador: GeneradorHashPbkdf2): List<FacturadorPruebaJson> =
    personal.map { FacturadorPruebaJson(it.usuario, it.nombre, generador.hash(it.clave)) }
