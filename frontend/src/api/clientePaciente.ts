import { ErrorApi, pedirJson, pedirSinRespuesta } from "./http";
import type { ErrorCampo, EstadoPrefactura, MotivoRechazoPdf, Pago, PrefacturaPaciente, TipoSoporte } from "./tipos";

export type ResultadoCarga = { readonly tipo: "cargado" } | { readonly tipo: "rechazado"; readonly motivo: MotivoRechazoPdf };

export type ResultadoRadicacion =
  | { readonly tipo: "radicada"; readonly consecutivo: string; readonly estado: EstadoPrefactura }
  | {
      readonly tipo: "invalida";
      readonly errores: Readonly<Record<string, ErrorCampo>>;
      readonly soportesFaltantes: readonly TipoSoporte[];
    };

type ErroresRadicacion = Omit<Extract<ResultadoRadicacion, { tipo: "invalida" }>, "tipo">;

export interface ClientePaciente {
  /** Devuelve el token, o null si la identificación y el ID de acceso no corresponden a una cita. */
  iniciarSesion(identificacion: string, codigoAcceso: string): Promise<string | null>;
  prefactura(token: string, senal: AbortSignal): Promise<PrefacturaPaciente>;
  subirSoporte(token: string, tipo: TipoSoporte, archivo: Blob): Promise<ResultadoCarga>;
  pagarCopago(token: string): Promise<Pago>;
  radicar(token: string, datos: Readonly<Record<string, string>>): Promise<ResultadoRadicacion>;
}

export function clientePacienteHttp(urlApi: string): ClientePaciente {
  const url = (ruta: string) => `${urlApi}/api/paciente${ruta}`;
  return {
    async iniciarSesion(identificacion, codigoAcceso) {
      try {
        const { token } = await pedirJson<{ token: string }>(url("/sesion"), { metodo: "POST", json: { identificacion, codigoAcceso } });
        return token;
      } catch (error) {
        if (error instanceof ErrorApi && error.codigo === "credenciales") return null;
        throw error;
      }
    },
    prefactura: (token, senal) => pedirJson(url("/prefactura"), { token, senal }),
    async subirSoporte(token, tipo, archivo) {
      try {
        await pedirSinRespuesta(url(`/soportes/${tipo}`), { metodo: "PUT", token, pdf: archivo });
        return { tipo: "cargado" };
      } catch (error) {
        if (error instanceof ErrorApi && error.estado === 422) return { tipo: "rechazado", motivo: error.codigo as MotivoRechazoPdf };
        throw error;
      }
    },
    pagarCopago: (token) => pedirJson(url("/copago"), { metodo: "POST", token }),
    async radicar(token, datos) {
      try {
        const radicada = await pedirJson<{ consecutivo: string; estado: EstadoPrefactura }>(url("/solicitud"), { metodo: "POST", token, json: { datos } });
        return { tipo: "radicada", ...radicada };
      } catch (error) {
        if (error instanceof ErrorApi && error.estado === 422) return { tipo: "invalida", ...(error.cuerpo as ErroresRadicacion) };
        throw error;
      }
    },
  };
}
