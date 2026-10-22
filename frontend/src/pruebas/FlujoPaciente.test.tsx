import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { clientePaciente, montar, pdf, PREFACTURA } from "./dobles";

async function ingresarHastaDatos(paciente = clientePaciente()) {
  montar({ clientes: { paciente } });
  await userEvent.type(await screen.findByLabelText(/Identificación de prueba/), "1000000001");
  await userEvent.type(screen.getByLabelText(/ID de prueba/), "PRUEBA1");
  await userEvent.click(screen.getByRole("button", { name: "Ingresar" }));
  await screen.findByRole("heading", { name: "[datos.titulo]" });
  return paciente;
}

describe("Flujo del paciente", () => {
  it("diligencia datos, carga los PDF, paga el copago y radica", async () => {
    const paciente = await ingresarHastaDatos();
    await userEvent.click(screen.getByRole("button", { name: "Continuar" }));
    expect(await screen.findByText("Falta el dato")).toBeInTheDocument();
    await userEvent.type(screen.getByLabelText(/Nombre del padre/), "Padre de prueba");
    await userEvent.click(screen.getByRole("button", { name: "Continuar" }));

    await screen.findByRole("heading", { name: "[soportes.titulo]" });
    await userEvent.click(screen.getByRole("button", { name: "Continuar" }));
    expect(screen.getByRole("alert")).toHaveTextContent("Faltan soportes");
    await userEvent.upload(screen.getByLabelText("Autorización"), pdf("foto", "foto.pdf"));
    expect(await screen.findByText("No es PDF")).toBeInTheDocument();
    await userEvent.upload(screen.getByLabelText("Autorización"), pdf());
    await userEvent.upload(screen.getByLabelText("Orden médica"), pdf());
    await userEvent.click(screen.getByRole("button", { name: "Continuar" }));

    await screen.findByRole("heading", { name: "[copago.titulo]" });
    await userEvent.click(screen.getByRole("button", { name: /Pagar/ }));
    await userEvent.click(await screen.findByRole("button", { name: "Enviar solicitud" }));

    expect(paciente.subirSoporte).toHaveBeenCalledTimes(2);
    expect(paciente.pagarCopago).toHaveBeenCalledWith("token");
    expect(paciente.radicar).toHaveBeenCalledWith("token", { "nombre-padre": "Padre de prueba" });
    expect(paciente.prefactura).toHaveBeenCalledTimes(2);
  });

  it("puede enviar sin pagar el copago", async () => {
    const paciente = await ingresarHastaDatos(clientePaciente({ prefactura: () => Promise.resolve({ ...PREFACTURA, soportes: ["AUTORIZACION", "ORDEN_MEDICA"], solicitud: null }) }));
    await userEvent.type(screen.getByLabelText(/Nombre del padre/), "Padre");
    await userEvent.click(screen.getByRole("button", { name: "Continuar" }));
    await userEvent.click(await screen.findByRole("button", { name: "Continuar" }));
    await userEvent.click(await screen.findByRole("button", { name: "Enviar sin pagar" }));
    expect(paciente.pagarCopago).not.toHaveBeenCalled();
    expect(paciente.radicar).toHaveBeenCalled();
  });

  it("muestra las novedades de una solicitud pendiente y permite corregirla con los datos anteriores", async () => {
    const pendiente = {
      ...PREFACTURA,
      solicitud: { consecutivo: "PF-000007", estado: "PENDIENTE" as const, fechaEnvio: "2026-10-22T13:00:00Z", datos: { "nombre-padre": "Anterior" }, novedades: [{ descripcion: "Falta la firma de la orden", fecha: "2026-10-22T14:00:00Z" }] },
    };
    montar({ clientes: { paciente: clientePaciente({ prefactura: vi.fn(() => Promise.resolve(pendiente)) }) } });
    await userEvent.type(await screen.findByLabelText(/Identificación de prueba/), "1");
    await userEvent.type(screen.getByLabelText(/ID de prueba/), "X");
    await userEvent.click(screen.getByRole("button", { name: "Ingresar" }));
    expect(await screen.findByText("PF-000007")).toBeInTheDocument();
    expect(screen.getByText("Falta la firma de la orden")).toBeInTheDocument();
    await userEvent.click(screen.getByRole("button", { name: "Corregir la solicitud" }));
    expect(await screen.findByLabelText(/Nombre del padre/)).toHaveValue("Anterior");
  });
});
