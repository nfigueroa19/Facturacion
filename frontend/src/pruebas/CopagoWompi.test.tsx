import { screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import type { ClientePaciente } from "../api/clientePaciente";
import type { PasarelaNavegador } from "../api/pasarelaWompi";
import type { CheckoutPago } from "../api/tipos";
import { clientePaciente, montar, PREFACTURA } from "./dobles";

const CHECKOUT: CheckoutPago = { llavePublica: "pub_test_prueba", referencia: "cita-1_0a0a", montoCentavos: 500_000, moneda: "COP", firma: "f".repeat(64) };
const prepararCopago = () => Promise.resolve({ pasarela: "CHECKOUT" as const, checkout: CHECKOUT });
const conSoportes = () => Promise.resolve({ ...PREFACTURA, soportes: ["AUTORIZACION" as const, "ORDEN_MEDICA" as const] });

async function irAlCopago(pagarCopago: ClientePaciente["pagarCopago"], pasarela: PasarelaNavegador) {
  montar({ clientes: { paciente: clientePaciente({ prepararCopago, pagarCopago, prefactura: conSoportes }), pasarela } });
  await userEvent.type(await screen.findByLabelText(/Identificación de prueba/), "1000000001");
  await userEvent.type(screen.getByLabelText(/ID de prueba/), "PRUEBA1");
  await userEvent.click(screen.getByRole("button", { name: "Ingresar" }));
  await userEvent.type(await screen.findByLabelText(/Nombre del padre/), "Padre");
  await userEvent.click(screen.getByRole("button", { name: "Continuar" }));
  await userEvent.click(await screen.findByRole("button", { name: "Continuar" }));
}

describe("Copago con Wompi sandbox", () => {
  it("abre el widget con los parámetros firmados y verifica en el backend la transacción que Wompi entrega", async () => {
    const pagarCopago = vi
      .fn<ClientePaciente["pagarCopago"]>()
      .mockResolvedValueOnce({ tipo: "rechazado", motivo: "RECHAZADA" })
      .mockResolvedValueOnce({ tipo: "pagado", pago: { referencia: "15113-2", valorPesos: 5000, fecha: "2026-10-24T13:00:00Z" } });
    let transaccion = 0;
    const abrir = vi.fn((_checkout: CheckoutPago, alTerminar: (id: string) => void) => {
      alTerminar(`15113-${++transaccion}`);
      return Promise.resolve();
    });
    await irAlCopago(pagarCopago, { abrir });

    await userEvent.click(await screen.findByRole("button", { name: "[copago.wompi.pagar]" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("[copago.rechazo.RECHAZADA]");
    expect(abrir).toHaveBeenLastCalledWith(CHECKOUT, expect.any(Function));
    await userEvent.click(screen.getByRole("button", { name: "[copago.wompi.pagar]" }));
    expect(await screen.findByRole("button", { name: "Enviar solicitud" })).toBeInTheDocument();
    expect(pagarCopago).toHaveBeenLastCalledWith("token", "15113-2");
  });

  it("si el widget no carga, avisa y deja reintentar sin llamar al backend", async () => {
    const pagarCopago = vi.fn<ClientePaciente["pagarCopago"]>();
    await irAlCopago(pagarCopago, { abrir: () => Promise.reject(new Error("sin red")) });
    await userEvent.click(await screen.findByRole("button", { name: "[copago.wompi.pagar]" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("[copago.wompi.error]");
    expect(screen.getByRole("button", { name: "[copago.wompi.pagar]" })).toBeEnabled();
    expect(pagarCopago).not.toHaveBeenCalled();
  });

  it("la ayuda lista las tarjetas y los celulares de prueba de Wompi y se cierra con Escape", async () => {
    await irAlCopago(vi.fn<ClientePaciente["pagarCopago"]>(), { abrir: () => Promise.resolve() });
    await userEvent.click(await screen.findByRole("button", { name: "[copago.wompi.ayuda.abrir]" }));
    const dialogo = await screen.findByRole("dialog");
    expect(within(dialogo).getByText("4242 4242 4242 4242")).toBeInTheDocument();
    expect(within(dialogo).getByText("4111 1111 1111 1111")).toBeInTheDocument();
    expect(within(dialogo).getByText("3991111111")).toBeInTheDocument();
    await userEvent.keyboard("{Escape}");
    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
  });
});
