import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { ErrorApi } from "../api/http";
import { clienteFacturador, DETALLE, montar, RESUMEN } from "./dobles";

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

  it("registra el ingreso de una aprobada y la factura de una lista para facturar", async () => {
    const aprobada = { ...DETALLE, resumen: { ...RESUMEN, estado: "APROBADO" as const } };
    const facturador = await ingresar(clienteFacturador({ detalle: vi.fn(() => Promise.resolve(aprobada)) }));
    await userEvent.click(await screen.findByRole("button", { name: "PF-000001" }));
    await userEvent.click(await screen.findByRole("button", { name: "[acciones.ingreso.boton]" }));
    expect(screen.getByText("[acciones.dato-vacio]")).toBeInTheDocument();
    await userEvent.type(screen.getByLabelText("[acciones.ingreso.etiqueta]"), "ING-77");
    await userEvent.click(screen.getByRole("button", { name: "[acciones.ingreso.boton]" }));
    expect(facturador.registrarIngreso).toHaveBeenCalledWith("token", "PF-000001", "ING-77");
  });

  it("muestra el motivo cuando la factura se intenta fuera del día de la cita", async () => {
    const ingreso = { valor: "ING-77", facturador: "facturador", fecha: "2026-10-22T13:00:00Z" };
    const lista = { ...DETALLE, resumen: { ...RESUMEN, estado: "LISTO_PARA_FACTURAR" as const }, atencion: { ...DETALLE.atencion, ingreso } };
    const fuera = new ErrorApi(409, "atencion", { error: "atencion", detalle: ["FUERA_DEL_DIA_DE_LA_CITA"] });
    const facturador = await ingresar(clienteFacturador({ detalle: vi.fn(() => Promise.resolve(lista)), registrarFactura: vi.fn(() => Promise.reject(fuera)) }));
    await userEvent.click(await screen.findByRole("button", { name: "PF-000001" }));
    expect(await screen.findByText("ING-77")).toBeInTheDocument();
    await userEvent.type(screen.getByLabelText("[acciones.factura.etiqueta]"), "FE-1");
    await userEvent.click(screen.getByRole("button", { name: "[acciones.factura.boton]" }));
    expect(await screen.findByText("[acciones.atencion.FUERA_DEL_DIA_DE_LA_CITA]")).toBeInTheDocument();
    expect(facturador.registrarFactura).toHaveBeenCalledWith("token", "PF-000001", "FE-1");
  });
});
