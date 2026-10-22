export type Textos = Readonly<Record<string, string>>;

export interface ClienteContenido {
  obtenerTextos(senal: AbortSignal): Promise<Textos>;
  urlImagen(clave: string): string;
}

function esTextos(valor: unknown): valor is Textos {
  return (
    typeof valor === "object" &&
    valor !== null &&
    !Array.isArray(valor) &&
    Object.values(valor).every((texto) => typeof texto === "string")
  );
}

export function clienteContenidoHttp(urlApi: string): ClienteContenido {
  return {
    async obtenerTextos(senal) {
      const respuesta = await fetch(`${urlApi}/api/contenido/textos`, { signal: senal });
      if (!respuesta.ok) throw new Error(`Textos no disponibles: HTTP ${respuesta.status}`);
      const cuerpo: unknown = await respuesta.json();
      if (!esTextos(cuerpo)) throw new Error("Textos con formato inesperado");
      return cuerpo;
    },
    urlImagen: (clave) => `${urlApi}/api/contenido/imagenes/${encodeURIComponent(clave)}`,
  };
}
