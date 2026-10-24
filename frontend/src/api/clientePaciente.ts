import { ErrorApi, pedirJson, pedirSinRespuesta } from "./http";
import type { ErrorCampo, EstadoPrefactura, IntencionPago, MotivoRechazoPago, MotivoRechazoPdf, Pago, PrefacturaPaciente, TipoSoporte } from "./tipos";

export type ResultadoPago = { readonly tipo: "pagado"; readonly pago: Pago } | { readonly tipo: "rechazado"; readonly motivo: MotivoRechazoPago };

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
  /** Cada llamada trae una referencia de pago nueva: Wompi no admite repetirla. */
  prepararCopago(token: string): Promise<IntencionPago>;
  /** `medio` es el ID de la transacción de Wompi o el token de la tarjeta de prueba; nunca el número de la tarjeta. */
  pagarCopago(token: string, medio: string): Promise<ResultadoPago>;
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
    prepararCopago: (token) => pedirJson(url("/copago/checkout"), { metodo: "POST", token }),
    async pagarCopago(token, medio) {
      try {
        return { tipo: "pagado", pago: await pedirJson<Pago>(url("/copago"), { metodo: "POST", token, json: { token: medio } }) };
      } catch (error) {
        // Sin motivo en el detalle, el rechazo se trata como falla de la pasarela.
        if (error instanceof ErrorApi && error.codigo === "pago-rechazado") return { tipo: "rechazado", motivo: (error.detalle[0] ?? "ERROR_PASARELA") as MotivoRechazoPago };
        throw error;
      }
    },
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

