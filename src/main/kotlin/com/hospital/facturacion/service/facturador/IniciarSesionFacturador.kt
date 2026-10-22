package com.hospital.facturacion.service.facturador

import com.hospital.facturacion.domain.facturador.DirectorioFacturadores
import com.hospital.facturacion.domain.facturador.Facturador
import com.hospital.facturacion.domain.sesion.Sesiones
import com.hospital.facturacion.domain.sesion.TokenSesion

data class SesionFacturador(val token: TokenSesion, val facturador: Facturador)

class IniciarSesionFacturador(
    private val directorio: DirectorioFacturadores,
    private val sesiones: Sesiones<Facturador>,
) {
    fun ejecutar(usuario: String, clave: String): SesionFacturador? =
        directorio.autenticar(usuario.trim(), clave)?.let { SesionFacturador(sesiones.abrir(it), it) }
}
