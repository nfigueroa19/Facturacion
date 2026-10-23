package com.hospital.facturacion.data.cita

import com.hospital.facturacion.data.appwrite.ConsultaAppwrite.Companion.igual
import com.hospital.facturacion.data.appwrite.TablaAppwrite
import com.hospital.facturacion.data.appwrite.idAppwrite
import com.hospital.facturacion.domain.cita.AgendaCitas
import com.hospital.facturacion.domain.cita.Cita
import com.hospital.facturacion.domain.cita.CitaId
import com.hospital.facturacion.domain.cita.Copago
import com.hospital.facturacion.domain.cita.Regimen
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate

/** Fila de la tabla `citas`. `fecha` va como AAAA-MM-DD: la cita es un día, no un instante. */
@Serializable
data class FilaCita(
    val cita: String,
    val identificacion: String,
    @SerialName("codigo_acceso") val codigoAcceso: String,
    val fecha: String,
    val medico: String,
    val especialidad: String,
    val regimen: Regimen,
    @SerialName("copago_pesos") val copagoPesos: Long?,
)

/** Agenda de prueba en Appwrite. En producción la reemplaza el adaptador al sistema del call center o a Dinámica (P-12). */
class AgendaCitasAppwrite(private val citas: TablaAppwrite<FilaCita>) : AgendaCitas {
    override fun buscar(id: CitaId): Cita? = citas.leer(idAppwrite(id.valor))?.cita()

    override fun buscarPorAcceso(identificacion: String, codigoAcceso: String): Cita? = citas
        .buscar(igual("identificacion", identificacion))
        .firstOrNull { it.codigoAcceso.igualA(codigoAcceso) }
        ?.cita()
}

/** Crea o actualiza las citas del archivo de arranque con la fecha calculada para hoy. */
fun sembrarCitas(citas: TablaAppwrite<FilaCita>, registradas: List<CitaRegistrada>) = registradas.forEach { (cita, codigo) ->
    citas.guardar(
        idAppwrite(cita.id.valor),
        FilaCita(cita.id.valor, cita.identificacionPaciente, codigo, cita.fecha.toString(), cita.medico, cita.especialidad, cita.regimen, cita.copago?.valorPesos),
    )
}

private fun FilaCita.cita() = Cita(
    id = requireNotNull(CitaId.de(cita)) { "Cita inválida en la tabla citas" },
    identificacionPaciente = identificacion,
    fecha = LocalDate.parse(fecha),
    medico = medico,
    especialidad = especialidad,
    regimen = regimen,
    copago = copagoPesos?.let(::Copago),
)
