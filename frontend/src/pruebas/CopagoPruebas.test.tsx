import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { clientePaciente, montar, PREFACTURA } from "./dobles";

describe("Pasarela de pruebas del copago", () => {
  it("muestra el rechazo de la tarjeta de prueba y deja reintentar con otra", async () => {
    const pagarCopago = vi
      .fn()
      .mockResolvedValueOnce({ tipo: "rechazado", motivo: "FONDOS_INSUFICIENTES" })
      .mockResolvedValueOnce({ tipo: "pagado", pago: { referencia: "SIM-9", valorPesos: 5000, fecha: "2026-10-22T13:00:00Z" } });
    const conSoportes = () => Promise.resolve({ ...PREFACTURA, soportes: ["AUTORIZACION" as const, "ORDEN_MEDICA" as const] });
    montar({ clientes: { paciente: clientePaciente({ pagarCopago, prefactura: conSoportes }) } });
    await userEvent.type(await screen.findByLabelText(/Identificación de prueba/), "1000000001");
    await userEvent.type(screen.getByLabelText(/ID de prueba/), "PRUEBA1");
    await userEvent.click(screen.getByRole("button", { name: "Ingresar" }));
    await userEvent.type(await screen.findByLabelText(/Nombre del padre/), "Padre");
    await userEvent.click(screen.getByRole("button", { name: "Continuar" }));
    await userEvent.click(await screen.findByRole("button", { name: "Continuar" }));

    await userEvent.click(await screen.findByRole("radio", { name: "[copago.tarjeta.tok_prueba_fondos_insuficientes]" }));
    await userEvent.click(screen.getByRole("button", { name: /Pagar/ }));
    expect(await screen.findByRole("alert")).toHaveTextContent("[copago.rechazo.FONDOS_INSUFICIENTES]");
    await userEvent.click(screen.getByRole("radio", { name: "[copago.tarjeta.tok_prueba_aprobada]" }));
    await userEvent.click(screen.getByRole("button", { name: /Pagar/ }));
    expect(await screen.findByRole("button", { name: "Enviar solicitud" })).toBeInTheDocument();
    expect(pagarCopago).toHaveBeenLastCalledWith("token", "tok_prueba_aprobada");
  });
});
