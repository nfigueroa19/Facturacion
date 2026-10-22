import { fireEvent, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { App } from "../App";
import type { ClienteContenido, Textos } from "../contenido/clienteContenido";
import { ProveedorContenido } from "../contenido/ContenidoContexto";

const TEXTOS: Textos = {
  "prefactura.titulo": "Prefactura de prueba",
  "acceso.campo-identificacion": "Identificación de prueba",
  "acceso.campo-id-acceso": "ID de prueba",
  "acceso.dato-obligatorio": "Dato obligatorio de prueba",
  "acceso.boton": "Ingresar",
  "acceso.servicio-pendiente": "Aviso de prueba",
};

function cliente(obtenerTextos: ClienteContenido["obtenerTextos"]): ClienteContenido {
  return { obtenerTextos, urlImagen: (clave) => `/imagenes/${clave}` };
}

function montar(c: ClienteContenido) {
  return render(
    <ProveedorContenido cliente={c}>
      <App />
    </ProveedorContenido>,
  );
}

describe("App", () => {
  it("muestra el esqueleto mientras carga y luego los textos del backend", async () => {
    montar(cliente(() => Promise.resolve(TEXTOS)));
    expect(screen.getByRole("status")).toHaveTextContent("Cargando la prefactura");
    expect(await screen.findByRole("heading", { name: "Prefactura de prueba" })).toBeInTheDocument();
  });

  it("marca entre corchetes una clave que falta en contenido.json", async () => {
    montar(cliente(() => Promise.resolve(TEXTOS)));
    expect(await screen.findByText("[acceso.ayuda]")).toBeInTheDocument();
  });

  it("ofrece reintentar cuando el backend no responde", async () => {
    const obtener = vi.fn<ClienteContenido["obtenerTextos"]>()
      .mockRejectedValueOnce(new Error("sin conexión"))
      .mockResolvedValueOnce(TEXTOS);
    montar(cliente(obtener));
    await userEvent.click(await screen.findByRole("button", { name: "Intentar de nuevo" }));
    expect(await screen.findByRole("heading", { name: "Prefactura de prueba" })).toBeInTheDocument();
  });

  it("vuelve a pedir el logo después de reintentar si había fallado", async () => {
    const obtener = vi.fn<ClienteContenido["obtenerTextos"]>()
      .mockRejectedValueOnce(new Error("sin conexión"))
      .mockResolvedValueOnce({ ...TEXTOS, "institucion.nombre": "Hospital de prueba" });
    montar(cliente(obtener));
    const reintentar = await screen.findByRole("button", { name: "Intentar de nuevo" });
    screen.getAllByRole("img").forEach((logo) => fireEvent.error(logo));
    expect(screen.queryAllByRole("img")).toHaveLength(0);
    await userEvent.click(reintentar);
    expect(await screen.findAllByRole("img", { name: "Hospital de prueba" })).toHaveLength(2);
  });

  it("valida los campos vacíos con el mensaje de contenido.json", async () => {
    montar(cliente(() => Promise.resolve(TEXTOS)));
    await userEvent.click(await screen.findByRole("button", { name: "Ingresar" }));
    expect(screen.getAllByText("Dato obligatorio de prueba")).toHaveLength(2);
    expect(screen.queryByText("Aviso de prueba")).not.toBeInTheDocument();
  });

  it("avisa que el ingreso es de prueba cuando los campos están completos", async () => {
    montar(cliente(() => Promise.resolve(TEXTOS)));
    await userEvent.type(await screen.findByLabelText(/Identificación de prueba/), "0000000");
    await userEvent.type(screen.getByLabelText(/ID de prueba/), "PRUEBA-01");
    await userEvent.click(screen.getByRole("button", { name: "Ingresar" }));
    expect(await screen.findByText("Aviso de prueba")).toBeInTheDocument();
  });
});
