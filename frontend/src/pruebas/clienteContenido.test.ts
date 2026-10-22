import { afterEach, describe, expect, it, vi } from "vitest";
import { clienteContenidoHttp } from "../contenido/clienteContenido";

const cliente = clienteContenidoHttp("http://backend.prueba");

function responder(cuerpo: unknown, status = 200) {
  vi.stubGlobal("fetch", vi.fn(() => Promise.resolve(new Response(JSON.stringify(cuerpo), { status }))));
}

describe("clienteContenidoHttp", () => {
  afterEach(() => vi.unstubAllGlobals());

  it("devuelve los textos cuando todos los valores son cadenas", async () => {
    responder({ titulo: "Prefactura" });
    await expect(cliente.obtenerTextos(new AbortController().signal)).resolves.toEqual({ titulo: "Prefactura" });
  });

  it.each([[["lista"]], [{ titulo: 3 }], [null]])("rechaza un cuerpo con forma inesperada: %j", async (cuerpo) => {
    responder(cuerpo);
    await expect(cliente.obtenerTextos(new AbortController().signal)).rejects.toThrow("formato inesperado");
  });

  it("rechaza una respuesta de error HTTP", async () => {
    responder({}, 500);
    await expect(cliente.obtenerTextos(new AbortController().signal)).rejects.toThrow("HTTP 500");
  });

  it("codifica la clave de la imagen en la URL", () => {
    expect(cliente.urlImagen("../x")).toBe("http://backend.prueba/api/contenido/imagenes/..%2Fx");
  });
});
