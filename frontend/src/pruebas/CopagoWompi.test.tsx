import { screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import type { ClientePaciente } from "../api/clientePaciente";
import { ErrorWompi, type PasarelaNavegador } from "../api/pasarelaWompi";
import { aceptarContratos, CHECKOUT, clientePaciente, montar, pagarConNequi, pasarela, PREFACTURA } from "./dobles";

const conSoportes = () => Promise.resolve({ ...PREFACTURA, soportes: ["AUTORIZACION" as const, "ORDEN_MEDICA" as const] });

async function irAlCopago(cambios: Partial<ClientePaciente>, navegador: PasarelaNavegador) {
  const paciente = clientePaciente({ prefactura: conSoportes, ...cambios });
  montar({ clientes: { paciente, pasarela: navegador } });
  await userEvent.type(await screen.findByLabelText(/Identificación de prueba/), "1000000001");
  await userEvent.type(screen.getByLabelText(/ID de prueba/), "PRUEBA1");
  await userEvent.click(screen.getByRole("button", { name: "Ingresar" }));
  await userEvent.type(await screen.findByLabelText(/Nombre del padre/), "Padre");
  await userEvent.click(screen.getByRole("button", { name: "Continuar" }));
  await userEvent.click(await screen.findByRole("button", { name: "Continuar" }));
  return paciente;
}

const campo = (nombre: string) => screen.getByLabelText(new RegExp(`copago\\.wompi\\.campo\\.${nombre}\\]`));

describe("Copago con Wompi sandbox", () => {
  it("paga con tarjeta, muestra el rechazo y reintenta con un checkout nuevo en cada intento", async () => {
    const pagarCopago = vi
      .fn<ClientePaciente["pagarCopago"]>()
      .mockResolvedValueOnce({ tipo: "rechazado", motivo: "RECHAZADA" })
      .mockResolvedValueOnce({ tipo: "pagado", pago: { referencia: "tx-2", valorPesos: 5000, fecha: "2026-10-24T13:00:00Z" } });
    const navegador = pasarela({ pagar: vi.fn<PasarelaNavegador["pagar"]>().mockResolvedValueOnce("tx-1").mockResolvedValueOnce("tx-2") });
    const paciente = await irAlCopago({ pagarCopago }, navegador);

    await userEvent.type(await screen.findByLabelText(/copago\.wompi\.campo\.numero\]/), "4242 4242 4242 4242");
    await userEvent.type(campo("titular"), "Paciente Prueba");
    await userEvent.type(campo("mes"), "5");
    await userEvent.type(campo("anio"), "29");
    await userEvent.type(campo("cvc"), "123");
    await userEvent.type(campo("correo"), "paciente@example.com");
    await aceptarContratos();
    await userEvent.click(screen.getByRole("button", { name: "[copago.wompi.pagar]" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("[copago.rechazo.RECHAZADA]");
    const tarjeta = { tipo: "CARD", numero: "4242424242424242", titular: "Paciente Prueba", mes: "05", anio: "29", cvc: "123" };
    expect(navegador.pagar).toHaveBeenLastCalledWith(CHECKOUT, tarjeta, "paciente@example.com");

    await userEvent.click(screen.getByRole("button", { name: "[copago.wompi.pagar]" }));
    expect(await screen.findByRole("button", { name: "Enviar solicitud" })).toBeInTheDocument();
    expect(pagarCopago).toHaveBeenLastCalledWith("token", "tx-2");
    expect(paciente.prepararCopago).toHaveBeenCalledTimes(3);
  });

  it("no crea la transacción sin aceptar los dos contratos de Wompi", async () => {
    const navegador = pasarela();
    await irAlCopago({}, navegador);
    await userEvent.click(await screen.findByRole("radio", { name: "[copago.wompi.medio.NEQUI]" }));
    await userEvent.type(campo("celular"), "3991111111");
    await userEvent.type(campo("correo"), "paciente@example.com");
    await userEvent.click(screen.getByRole("checkbox", { name: "[copago.wompi.aceptacion.terminos]" }));
    await userEvent.click(screen.getByRole("button", { name: "[copago.wompi.pagar]" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("[copago.wompi.aceptacion.falta]");
    expect(navegador.pagar).not.toHaveBeenCalled();
    expect(screen.getByRole("link", { name: "[copago.wompi.aceptacion.leer-datos]" })).toHaveAttribute("href", "https://wompi.test/datos.pdf");
  });

  it("distingue los datos rechazados por Wompi de una falla de conexión y no llama al backend", async () => {
    const pagarCopago = vi.fn<ClientePaciente["pagarCopago"]>();
    const pagar = vi.fn<PasarelaNavegador["pagar"]>().mockRejectedValueOnce(new ErrorWompi("422")).mockRejectedValueOnce(new TypeError("sin red"));
    await irAlCopago({ pagarCopago }, pasarela({ pagar }));
    await pagarConNequi();
    expect(await screen.findByRole("alert")).toHaveTextContent("[copago.wompi.datos-invalidos]");
    await userEvent.click(screen.getByRole("button", { name: "[copago.wompi.pagar]" }));
    expect(await screen.findByText("[copago.wompi.error]")).toBeInTheDocument();
    expect(pagarCopago).not.toHaveBeenCalled();
  });

  it("con PSE carga los bancos del sandbox con la llave pública del checkout", async () => {
    const navegador = pasarela();
    await irAlCopago({}, navegador);
    await userEvent.click(await screen.findByRole("radio", { name: "[copago.wompi.medio.PSE]" }));
    await userEvent.click(await screen.findByRole("button", { name: /copago\.wompi\.campo\.banco\]/ }));
    expect(await screen.findByRole("option", { name: "Banco que aprueba" })).toBeInTheDocument();
    expect(navegador.bancosPse).toHaveBeenCalledWith("pub_test_prueba");
  });

  it("la ayuda lista las tarjetas y los celulares de prueba de Wompi y se cierra con Escape", async () => {
    await irAlCopago({}, pasarela());
    await userEvent.click(await screen.findByRole("button", { name: "[copago.wompi.ayuda.abrir]" }));
    const dialogo = await screen.findByRole("dialog");
    expect(within(dialogo).getByText("4242 4242 4242 4242")).toBeInTheDocument();
    expect(within(dialogo).getByText("4111 1111 1111 1111")).toBeInTheDocument();
    expect(within(dialogo).getByText("3991111111")).toBeInTheDocument();
    await userEvent.keyboard("{Escape}");
    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
  });
});
