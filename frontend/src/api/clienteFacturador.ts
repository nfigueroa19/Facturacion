import { ErrorApi, pedirArchivo, pedirJson, pedirSinRespuesta } from "./http";
import type { DetalleSolicitud, EstadoPrefactura, ItemRevision, ResumenSolicitud, TipoSoporte } from "./tipos";

export interface SesionFacturador {
  readonly token: string;
  readonly nombre: string;
}

export interface ClienteFacturador {
  /** Devuelve null si el usuario o la clave no son correctos. */
  iniciarSesion(usuario: string, clave: string): Promise<SesionFacturador | null>;
  solicitudes(token: string, estado: EstadoPrefactura | null, senal: AbortSignal): Promise<readonly ResumenSolicitud[]>;
  detalle(token: string, consecutivo: string, senal: AbortSignal): Promise<DetalleSolicitud>;
  soporte(token: string, consecutivo: string, tipo: TipoSoporte, senal: AbortSignal): Promise<Blob>;
  aprobar(token: string, consecutivo: string, revisados: readonly ItemRevision[]): Promise<void>;
  registrarNovedad(token: string, consecutivo: string, descripcion: string): Promise<void>;
  confirmarLlegada(token: string, consecutivo: string): Promise<void>;
  registrarIngreso(token: string, consecutivo: string, numero: string): Promise<void>;
  anularIngreso(token: string, consecutivo: string, motivo: string): Promise<void>;
  registrarIntentoFallido(token: string, consecutivo: string, error: string): Promise<void>;
  registrarFactura(token: string, consecutivo: string, numero: string): Promise<void>;
}

export function clienteFacturadorHttp(urlApi: string): ClienteFacturador {
  const url = (ruta: string) => `${urlApi}/api/facturador${ruta}`;
  const solicitud = (consecutivo: string, ruta = "") => url(`/solicitudes/${encodeURIComponent(consecutivo)}${ruta}`);
  return {
    async iniciarSesion(usuario, clave) {
      try {
        return await pedirJson<SesionFacturador>(url("/sesion"), { metodo: "POST", json: { usuario, clave } });
      } catch (error) {
        if (error instanceof ErrorApi && error.codigo === "credenciales") return null;
        throw error;
      }
    },
    solicitudes: (token, estado, senal) =>
      pedirJson(url(estado === null ? "/solicitudes" : `/solicitudes?estado=${estado}`), { token, senal }),
    detalle: (token, consecutivo, senal) => pedirJson(solicitud(consecutivo), { token, senal }),
    soporte: (token, consecutivo, tipo, senal) => pedirArchivo(solicitud(consecutivo, `/soportes/${tipo}`), { token, senal }),
    aprobar: (token, consecutivo, revisados) =>
      pedirSinRespuesta(solicitud(consecutivo, "/aprobar"), { metodo: "POST", token, json: { revisados } }),
    registrarNovedad: (token, consecutivo, descripcion) =>
      pedirSinRespuesta(solicitud(consecutivo, "/novedades"), { metodo: "POST", token, json: { descripcion } }),
    confirmarLlegada: (token, consecutivo) => pedirSinRespuesta(solicitud(consecutivo, "/llegada"), { metodo: "POST", token }),
    registrarIngreso: (token, consecutivo, numero) =>
      pedirSinRespuesta(solicitud(consecutivo, "/ingreso"), { metodo: "POST", token, json: { numero } }),
    anularIngreso: (token, consecutivo, motivo) =>
      pedirSinRespuesta(solicitud(consecutivo, "/anulacion"), { metodo: "POST", token, json: { texto: motivo } }),
    registrarIntentoFallido: (token, consecutivo, error) =>
      pedirSinRespuesta(solicitud(consecutivo, "/intentos-facturacion"), { metodo: "POST", token, json: { texto: error } }),
    registrarFactura: (token, consecutivo, numero) =>
      pedirSinRespuesta(solicitud(consecutivo, "/factura"), { metodo: "POST", token, json: { numero } }),
  };
}
