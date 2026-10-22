import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from "react";
import type { ClienteContenido, Textos } from "./clienteContenido";

export type EstadoContenido =
  | { readonly tipo: "cargando" }
  | { readonly tipo: "listo"; readonly textos: Textos }
  | { readonly tipo: "error" };

interface Contenido {
  readonly estado: EstadoContenido;
  readonly reintentar: () => void;
  readonly urlImagen: (clave: string) => string;
}

const ContextoContenido = createContext<Contenido | null>(null);

export function ProveedorContenido({ cliente, children }: { cliente: ClienteContenido; children: ReactNode }) {
  const [estado, setEstado] = useState<EstadoContenido>({ tipo: "cargando" });
  const [intento, setIntento] = useState(0);

  useEffect(() => {
    const control = new AbortController();
    setEstado({ tipo: "cargando" });
    cliente
      .obtenerTextos(control.signal)
      .then((textos) => setEstado({ tipo: "listo", textos }))
      .catch(() => {
        if (!control.signal.aborted) setEstado({ tipo: "error" });
      });
    return () => control.abort();
  }, [cliente, intento]);

  const reintentar = useCallback(() => setIntento((n) => n + 1), []);
  return (
    <ContextoContenido.Provider value={{ estado, reintentar, urlImagen: cliente.urlImagen }}>
      {children}
    </ContextoContenido.Provider>
  );
}

export function useContenido(): Contenido {
  const contenido = useContext(ContextoContenido);
  if (contenido === null) throw new Error("useContenido requiere ProveedorContenido");
  return contenido;
}

/** Texto por clave de config/contenido.json. Una clave faltante se muestra entre corchetes para que el personal la detecte. */
export function textoDe(textos: Textos, clave: string): string {
  return textos[clave] ?? `[${clave}]`;
}

export type Texto = (clave: string) => string;

export const lectorDe =
  (textos: Textos): Texto =>
  (clave) =>
    textoDe(textos, clave);
