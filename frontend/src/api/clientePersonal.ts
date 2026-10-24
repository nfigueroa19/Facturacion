import { ErrorApi, pedirJson } from "./http";
import type { CitaProgramada, Comunicacion, ErrorNuevaCita, NuevaCita, Turno } from "./tipos";

export type ResultadoCita =
  | { readonly tipo: "programada"; readonly programada: CitaProgramada }
  | { readonly tipo: "invalida"; readonly errores: readonly ErrorNuevaCita[] };

/** Call center, digiturno y panel de comunicaciones. Usan el token de la sesión del personal. */
export interface ClientePersonal {
  programarCita(token: string, cita: NuevaCita): Promise<ResultadoCita>;
  atenderLlegada(token: string, identificacion: string): Promise<Turno>;
  comunicaciones(token: string, senal: AbortSignal): Promise<readonly Comunicacion[]>;
}

export function clientePersonalHttp(urlApi: string): ClientePersonal {
  return {
    async programarCita(token, cita) {
      try {
        const programada = await pedirJson<CitaProgramada>(`${urlApi}/api/callcenter/citas`, { metodo: "POST", token, json: cita });
        return { tipo: "programada", programada };
      } catch (error) {
        if (error instanceof ErrorApi && error.estado === 422) return { tipo: "invalida", ...(error.cuerpo as { errores: readonly ErrorNuevaCita[] }) };
        throw error;
      }
    },
    atenderLlegada: (token, identificacion) => pedirJson(`${urlApi}/api/digiturno/llegadas`, { metodo: "POST", token, json: { identificacion } }),
    comunicaciones: (token, senal) => pedirJson(`${urlApi}/api/personal/comunicaciones`, { token, senal }),
  };
}
