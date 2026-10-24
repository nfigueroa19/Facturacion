import { screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { CATALOGO, clientePruebas, montar } from "./dobles";

const conCatalogo = (restablecer = vi.fn(() => Promise.resolve(10))) =>
  clientePruebas({ catalogo: vi.fn(() => Promise.resolve(CATALOGO)), restablecer });

async function ingresarPersonal(pruebas = conCatalogo()) {
  montar({ clientes: { pruebas }, ruta: "/facturador" });
  await userEvent.type(await screen.findByLabelText(/Usuario/), "facturador");
  await userEvent.type(screen.getByLabelText(/Clave/), "prueba123");
  await userEvent.click(screen.getByRole("button", { name: "Ingresar" }));
  return pruebas;
}

describe("Datos de prueba", () => {
  it("sin catálogo publicado el paciente no ve el botón", async () => {
    montar();
    expect(await screen.findByRole("button", { name: "Ingresar" })).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Ver datos de prueba" })).not.toBeInTheDocument();
  });

  it("sin catálogo publicado el personal no ve el restablecimiento", async () => {
    await ingresarPersonal(clientePruebas());
    expect(await screen.findByRole("button", { name: /comunicaciones.abrir/ })).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Restablecer datos de prueba" })).not.toBeInTheDocument();
  });

  it("el paciente abre el catálogo en la pestaña de pacientes y ve identificación e ID de acceso", async () => {
    montar({ clientes: { pruebas: conCatalogo() } });
    await userEvent.click(await screen.findByRole("button", { name: "Ver datos de prueba" }));
    const modal = await screen.findByRole("dialog");
    expect(within(modal).getByRole("tab", { name: "[pruebas.pestana.pacientes]" })).toHaveAttribute("aria-selected", "true");
    expect(within(modal).getByText("Flujo feliz de prueba")).toBeInTheDocument();
    expect(within(modal).getByText("1111111111")).toBeInTheDocument();
    expect(within(modal).getByText("FELIZ")).toBeInTheDocument();
    await userEvent.click(within(modal).getByRole("tab", { name: "[pruebas.pestana.personal]" }));
    expect(within(modal).getByText("clave-de-prueba")).toBeInTheDocument();
    await userEvent.keyboard("{Escape}");
    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
  });

  it("el personal confirma el restablecimiento y ve cuántos casos volvieron al inicio", async () => {
    const pruebas = await ingresarPersonal();
    await userEvent.click(await screen.findByRole("button", { name: "Restablecer datos de prueba" }));
    await userEvent.click(within(await screen.findByRole("dialog")).getByRole("button", { name: "Restablecer" }));
    expect(await screen.findByText("Listo: 10 casos restablecidos")).toBeInTheDocument();
    expect(pruebas.restablecer).toHaveBeenCalledWith("token");
  });

  it("un fallo al restablecer muestra el error y permite reintentar", async () => {
    await ingresarPersonal(conCatalogo(vi.fn(() => Promise.reject(new Error("sin red")))));
    await userEvent.click(await screen.findByRole("button", { name: "Restablecer datos de prueba" }));
    await userEvent.click(within(await screen.findByRole("dialog")).getByRole("button", { name: "Restablecer" }));
    expect(await screen.findByText("[pruebas.restablecer.error]")).toBeInTheDocument();
    expect(within(screen.getByRole("dialog")).getByRole("button", { name: "Restablecer" })).toBeEnabled();
  });
});
