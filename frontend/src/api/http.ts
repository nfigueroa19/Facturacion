/** Respuesta no exitosa del backend. `codigo` es el campo `error` de `ErrorDto`, si vino. */
export class ErrorApi extends Error {
  constructor(
    readonly estado: number,
    readonly codigo: string | null,
    readonly cuerpo: unknown,
  ) {
    super(`HTTP ${estado}${codigo === null ? "" : ` ${codigo}`}`);
  }
}

export const esSesionVencida = (error: unknown) => error instanceof ErrorApi && error.estado === 401;

interface Peticion {
  readonly metodo?: "GET" | "POST" | "PUT";
  readonly token?: string;
  readonly json?: unknown;
  readonly pdf?: Blob;
  readonly senal?: AbortSignal;
}

async function leerCuerpo(respuesta: Response): Promise<unknown> {
  try {
    return await respuesta.json();
  } catch {
    return null;
  }
}

async function pedir(url: string, peticion: Peticion): Promise<Response> {
  const cabeceras = new Headers();
  if (peticion.token !== undefined) cabeceras.set("Authorization", `Bearer ${peticion.token}`);
  let cuerpo: BodyInit | undefined;
  if (peticion.json !== undefined) {
    cabeceras.set("Content-Type", "application/json");
    cuerpo = JSON.stringify(peticion.json);
  } else if (peticion.pdf !== undefined) {
    cabeceras.set("Content-Type", "application/pdf");
    cuerpo = peticion.pdf;
  }
  const respuesta = await fetch(url, { method: peticion.metodo ?? "GET", headers: cabeceras, body: cuerpo, signal: peticion.senal });
  if (respuesta.ok) return respuesta;
  const error = await leerCuerpo(respuesta);
  const codigo = typeof error === "object" && error !== null && "error" in error && typeof error.error === "string" ? error.error : null;
  throw new ErrorApi(respuesta.status, codigo, error);
}

// El backend vive en el mismo repositorio y es dueño del contrato; sus pruebas fijan la forma de cada DTO.
export async function pedirJson<T>(url: string, peticion: Peticion = {}): Promise<T> {
  return (await (await pedir(url, peticion)).json()) as T;
}

export async function pedirSinRespuesta(url: string, peticion: Peticion): Promise<void> {
  await pedir(url, peticion);
}

export async function pedirArchivo(url: string, peticion: Peticion): Promise<Blob> {
  return (await pedir(url, peticion)).blob();
}
