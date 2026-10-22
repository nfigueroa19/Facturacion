import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { clienteFacturador, montar } from "./dobles";

beforeEach(() => {
  // jsdom no implementa URLs blob:, que el visor usa para mostrar el PDF.
  URL.createObjectURL = vi.fn(() => "blob:prueba");
  URL.revokeObjectURL = vi.fn();
});

async function ingresar(facturador = clienteFacturador()) {
  montar({ clientes: { facturador }, ruta: "/facturador" });
  await userEvent.type(await screen.findByLabelText(/Usuario/), "facturador");
  await userEvent.type(screen.getByLabelText(/Clave/), "prueba123");
  await userEvent.click(screen.getByRole("button", { name: "Ingresar" }));
  return facturador;
}

describe("Panel del facturador", () => {
  it("lista la cola en revisión y abre el detalle con el PDF", async () => {
    const facturador = await ingresar();
    expect(facturador.solicitudes).toHaveBeenCalledWith("token", "EN_REVISION", expect.any(AbortSignal));
    await userEvent.click(await screen.findByRole("button", { name: "PF-000001" }));
    expect(await screen.findByText("Padre de prueba")).toBeInTheDocument();
    expect(await screen.findByTitle(/Autorización PF-000001/)).toHaveAttribute("src", "blob:prueba");
  });

  it("filtra por identificación sin volver a pedir la lista", async () => {
    const facturador = await ingresar();
    await screen.findByRole("button", { name: "PF-000001" });
    await userEvent.type(screen.getByLabelText("[solicitudes.filtro-texto]"), "999");
    expect(screen.queryByRole("button", { name: "PF-000001" })).not.toBeInTheDocument();
    expect(facturador.solicitudes).toHaveBeenCalledTimes(1);
  });

  it("aprueba solo con el checklist completo", async () => {
    const facturador = await ingresar();
    await userEvent.click(await screen.findByRole("button", { name: "PF-000001" }));
    await userEvent.click(await screen.findByRole("button", { name: "Aprobar solicitud" }));
    expect(screen.getByText("Checklist incompleto")).toBeInTheDocument();
    expect(facturador.aprobar).not.toHaveBeenCalled();
    for (const item of ["Datos revisados", "Autorización revisada", "Orden revisada"]) await userEvent.click(screen.getByRole("checkbox", { name: item }));
    await userEvent.click(screen.getByRole("button", { name: "Aprobar solicitud" }));
    expect(facturador.aprobar).toHaveBeenCalledWith("token", "PF-000001", ["DATOS", "AUTORIZACION", "ORDEN_MEDICA"]);
  });
});
