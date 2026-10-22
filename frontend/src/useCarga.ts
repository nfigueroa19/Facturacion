import { useCallback, useEffect, useState } from "react";

export type Carga<T> =
  | { readonly tipo: "cargando" }
  | { readonly tipo: "error"; readonly error: unknown }
  | { readonly tipo: "lista"; readonly valor: T };

/**
 * Pide datos al montar y cada vez que cambian `claves`, y cancela la petición anterior.
 * `recargar` conserva lo que ya se ve mientras llega la respuesta, para que la pantalla no salte al esqueleto.
 */
export function useCarga<T>(pedir: (senal: AbortSignal) => Promise<T>, claves: readonly unknown[]) {
  const clave = JSON.stringify(claves);
  const [carga, setCarga] = useState<Carga<T>>({ tipo: "cargando" });
  const [claveActual, setClaveActual] = useState(clave);
  const [intento, setIntento] = useState(0);

  if (clave !== claveActual) {
    setClaveActual(clave);
    setCarga({ tipo: "cargando" });
  }

  useEffect(() => {
    const control = new AbortController();
    pedir(control.signal)
      .then((valor) => setCarga({ tipo: "lista", valor }))
      .catch((error: unknown) => {
        if (!control.signal.aborted) setCarga({ tipo: "error", error });
      });
    return () => control.abort();
    // `pedir` cambia en cada render; lo que decide cuándo volver a pedir son la clave y el intento.
  }, [clave, intento]);

  const recargar = useCallback(() => {
    setCarga((actual) => (actual.tipo === "error" ? { tipo: "cargando" } : actual));
    setIntento((n) => n + 1);
  }, []);

  return { carga, recargar };
}
