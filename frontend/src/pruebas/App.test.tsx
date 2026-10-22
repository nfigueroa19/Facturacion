import { fireEvent, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import type { ClienteContenido } from "../contenido/clienteContenido";
import { clientePaciente, contenido, montar, TEXTOS } from "./dobles";

async function ingresar() {
  await userEvent.type(await screen.findByLabelText(/Identificación de prueba/), "1000000001");
  await userEvent.type(screen.getByLabelText(/ID de prueba/), "PRUEBA1");
  await userEvent.click(screen.getByRole("button", { name: "Ingresar" }));
}

describe("App", () => {
  it("muestra el esqueleto mientras carga y luego los textos del backend", async () => {
    montar();
    expect(screen.getByRole("status")).toHaveTextContent("Cargando la prefactura");
    expect(await screen.findByRole("heading", { name: "Prefactura de prueba" })).toBeInTheDocument();
  });

  it("marca entre corchetes una clave que falta en contenido.json", async () => {
    montar();
    expect(await screen.findByText("[acceso.ayuda]")).toBeInTheDocument();
  });

  it("ofrece reintentar cuando el backend no responde", async () => {
    const obtener = vi.fn<ClienteContenido["obtenerTextos"]>().mockRejectedValueOnce(new Error("sin conexión")).mockResolvedValueOnce(TEXTOS);
    montar({ contenido: contenido(obtener) });
    await userEvent.click(await screen.findByRole("button", { name: "Intentar de nuevo" }));
    expect(await screen.findByRole("heading", { name: "Prefactura de prueba" })).toBeInTheDocument();
  });

  it("vuelve a pedir el logo después de reintentar si había fallado", async () => {
    const obtener = vi
      .fn<ClienteContenido["obtenerTextos"]>()
      .mockRejectedValueOnce(new Error("sin conexión"))
      .mockResolvedValueOnce({ ...TEXTOS, "institucion.nombre": "Hospital de prueba" });
    montar({ contenido: contenido(obtener) });
    const reintentar = await screen.findByRole("button", { name: "Intentar de nuevo" });
    screen.getAllByRole("img").forEach((logo) => fireEvent.error(logo));
    expect(screen.queryAllByRole("img")).toHaveLength(0);
    await userEvent.click(reintentar);
    expect(await screen.findAllByRole("img", { name: "Hospital de prueba" })).toHaveLength(2);
  });

  it("valida los campos vacíos con el mensaje de contenido.json sin llamar al backend", async () => {
    const paciente = clientePaciente();
    montar({ clientes: { paciente } });
    await userEvent.click(await screen.findByRole("button", { name: "Ingresar" }));
    expect(screen.getAllByText("Dato obligatorio de prueba")).toHaveLength(2);
    expect(paciente.iniciarSesion).not.toHaveBeenCalled();
  });

  it("avisa cuando la identificación y el ID de acceso no coinciden con una cita", async () => {
    montar({ clientes: { paciente: clientePaciente({ iniciarSesion: () => Promise.resolve(null) }) } });
    await ingresar();
    expect(await screen.findByRole("alert")).toHaveTextContent("Credenciales erradas de prueba");
  });

  it("con credenciales válidas abre el primer paso de la prefactura", async () => {
    const paciente = clientePaciente();
    montar({ clientes: { paciente } });
    await ingresar();
    expect(paciente.iniciarSesion).toHaveBeenCalledWith("1000000001", "PRUEBA1");
    expect(await screen.findByRole("heading", { name: "[datos.titulo]" })).toBeInTheDocument();
  });
});
