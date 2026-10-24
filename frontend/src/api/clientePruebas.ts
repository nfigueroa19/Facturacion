import { ErrorApi, pedirJson } from "./http";
import type { CatalogoPruebas } from "./tipos";

/** Escenarios de prueba del piloto: el catálogo es público; restablecer usa la sesión del personal. */
export interface ClientePruebas {
  /** null cuando el backend no tiene activos los escenarios (404). */
  catalogo(senal: AbortSignal): Promise<CatalogoPruebas | null>;
  /** Devuelve cuántos escenarios quedaron en su estado inicial. */
  restablecer(token: string): Promise<number>;
}

export function clientePruebasHttp(urlApi: string): ClientePruebas {
  return {
    async catalogo(senal) {
      try {
        return await pedirJson<CatalogoPruebas>(`${urlApi}/api/dev/catalogo`, { senal });
      } catch (error) {
        if (error instanceof ErrorApi && error.estado === 404) return null;
        throw error;
      }
    },
    async restablecer(token) {
      return (await pedirJson<{ escenarios: number }>(`${urlApi}/api/dev/poblar`, { metodo: "POST", token })).escenarios;
    },
  };
}
