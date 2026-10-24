import { screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import type { Comunicacion } from "../api/tipos";
import { clientePersonal, montar } from "./dobles";

async function ingresar(ruta: string, personal = clientePersonal()) {
  montar({ clientes: { personal }, ruta });
  await userEvent.type(await screen.findByLabelText(/Usuario/), "facturador");
  await userEvent.type(screen.getByLabelText(/Clave/), "prueba123");
  await userEvent.click(screen.getByRole("button", { name: "Ingresar" }));
  return personal;
}

const SMS: Comunicacion = {
  cita: "cita-1",
  motivo: "CITA_ASIGNADA",
  destino: "3000000099",
  texto: "Su ID de acceso es ABCD2345",
  estado: "SIMULADO",
  canal: "simulado",
  fecha: "2026-10-22T13:00:00Z",
};

describe("Módulos del personal", () => {
  it("el call center programa la cita y muestra el ID de acceso generado", async () => {
    const personal = await ingresar("/callcenter");
    await userEvent.type(await screen.findByLabelText(/callcenter.campo.identificacion/), "1000000099");
    await userEvent.type(screen.getByLabelText(/callcenter.campo.celular/), "3000000099");
    await userEvent.type(screen.getByLabelText(/callcenter.campo.medico/), "Médico");
    await userEvent.type(screen.getByLabelText(/callcenter.campo.especialidad/), "General");
    await userEvent.click(screen.getByRole("button", { name: "[callcenter.programar]" }));
    expect(await screen.findByText("ABCD2345")).toBeInTheDocument();
    expect(personal.programarCita).toHaveBeenCalledWith("token", expect.objectContaining({ identificacion: "1000000099", regimen: "CONTRIBUTIVO", copagoPesos: null }));
  });

  it("marca en su campo el dato que el backend rechazó", async () => {
    await ingresar("/callcenter", clientePersonal({ programarCita: vi.fn(() => Promise.resolve({ tipo: "invalida" as const, errores: ["CELULAR" as const] })) }));
    await userEvent.click(await screen.findByRole("button", { name: "[callcenter.programar]" }));
    expect(await screen.findByText("[callcenter.error.CELULAR]")).toBeInTheDocument();
  });

  it("el digiturno asigna turno y la sesión sigue al pasar a otro módulo", async () => {
    const personal = await ingresar("/digiturno");
    await userEvent.type(await screen.findByLabelText(/Identificación de prueba/), "1000000001");
    await userEvent.click(screen.getByRole("button", { name: "[digiturno.pedir]" }));
    expect(await screen.findByText("P-001")).toBeInTheDocument();
    expect(personal.atenderLlegada).toHaveBeenCalledWith("token", "1000000001");
    await userEvent.click(screen.getByRole("link", { name: "[callcenter.panel]" }));
    expect(await screen.findByRole("heading", { name: "[callcenter.titulo]" })).toBeInTheDocument();
  });

  it("el panel de comunicaciones lista los SMS simulados", async () => {
    await ingresar("/callcenter", clientePersonal({ comunicaciones: vi.fn(() => Promise.resolve([SMS])) }));
    await userEvent.click(await screen.findByRole("button", { name: /comunicaciones.abrir/ }));
    const panel = await screen.findByRole("dialog");
    expect(await within(panel).findByText("Su ID de acceso es ABCD2345")).toBeInTheDocument();
    expect(within(panel).getByText("[comunicaciones.estado.SIMULADO]")).toBeInTheDocument();
  });
});
